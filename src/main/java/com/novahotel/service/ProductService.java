package com.novahotel.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novahotel.dto.ProductDTO;
import com.novahotel.dto.UpdateProductRequest;
import com.novahotel.entity.Category;
import com.novahotel.entity.Product;
import com.novahotel.entity.ProductImage;
import com.novahotel.repository.CategoryRepository;
import com.novahotel.repository.ProductImageRepository;
import com.novahotel.repository.ProductRepository;

@Service
@Transactional
public class ProductService {
	private final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Value("${imagekit.privateKey}")
    private String imagekitPrivateKey;

    @Value("${imagekit.urlEndpoint}")
    private String imagekitUrlEndpoint;

    @Value("${imagekit.folder:/products}")
    private String imagekitFolder;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    /* =====================================================
     * ImageKit upload helpers
     * ===================================================== */

    private static class UploadResult {
        String url;
        String filePath;

        UploadResult(String url, String filePath) {
            this.url = url;
            this.filePath = filePath;
        }
    }
    
   

    private UploadResult uploadToImageKit(MultipartFile file) throws IOException, InterruptedException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String boundary = "----ImageKitBoundary" + System.currentTimeMillis();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
        writer.append("Content-Type: ").append(file.getContentType()).append("\r\n\r\n");
        writer.flush();
        baos.write(file.getBytes());
        baos.write("\r\n".getBytes());

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"fileName\"\r\n\r\n");
        writer.append(fileName).append("\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"folder\"\r\n\r\n");
        writer.append(imagekitFolder).append("\r\n");

        writer.append("--").append(boundary).append("--\r\n");
        writer.close();

        String auth = Base64.getEncoder()
                .encodeToString((imagekitPrivateKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imagekit.io/v1/files/upload"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode node = new ObjectMapper().readTree(response.body());
            String url = node.get("url").asText();
            String filePath = node.get("filePath").asText();
            return new UploadResult(url, filePath);
        }

        throw new RuntimeException("ImageKit upload failed: " + response.body());
    }

    private void deleteFromImageKit(String filePath) {
        try {
            if (filePath == null) return;

            String auth = Base64.getEncoder()
                    .encodeToString((imagekitPrivateKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.imagekit.io/v1/files/delete"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"filePath\":\"" + filePath + "\"}"
                    ))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            log.warn("ImageKit delete failed: {}", ex.getMessage());
        }
    }

    /* =====================================================
     * Cover Image
     * ===================================================== */

    public Product attachCoverImage(Long productId, MultipartFile file)
            throws IOException, InterruptedException {

        Product product = getProductById(productId);

        productImageRepository.findByProductIdAndIsCoverTrue(productId)
                .ifPresent(old -> {
                    deleteFromImageKit(old.getFilePath());
                    productImageRepository.delete(old);
                });

        UploadResult res = uploadToImageKit(file);

        ProductImage cover = new ProductImage();
        cover.setProduct(product);
        cover.setUrl(res.url);
        cover.setFilePath(res.filePath);
        cover.setIsCover(true);
        productImageRepository.save(cover);

        product.setImageUrl(res.url);
        return productRepository.save(product);
    }

    /* =====================================================
     * Gallery Images
     * ===================================================== */

    public Product addAdditionalImages(Long productId, List<MultipartFile> files)
            throws IOException, InterruptedException {

        Product product = getProductById(productId);

        for (MultipartFile file : files) {
            UploadResult res = uploadToImageKit(file);

            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl(res.url);
            img.setFilePath(res.filePath);
            img.setIsCover(false);

            productImageRepository.save(img);
        }
        return product;
    }

    public List<String> getAdditionalImages(Long productId) {
        return productImageRepository.findByProductId(productId).stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsCover()))
                .map(ProductImage::getUrl)
                .toList();
    }

    public Product removeImage(Long productId, String imageUrl) {

        Product product = getProductById(productId);

        productImageRepository.findByUrl(imageUrl).ifPresent(img -> {
            deleteFromImageKit(img.getFilePath());
            productImageRepository.delete(img);

            if (Boolean.TRUE.equals(img.getIsCover())) {
                product.setImageUrl(null);
                productRepository.save(product);
            }
        });

        return product;
    }

    public Product uploadCoverAndGallery(
            Long productId,
            MultipartFile cover,
            List<MultipartFile> gallery)
            throws IOException, InterruptedException {

        if (cover != null && !cover.isEmpty()) {
            attachCoverImage(productId, cover);
        }

        if (gallery != null && !gallery.isEmpty()) {
            addAdditionalImages(productId, gallery);
        }

        return getProductById(productId);
    }

    /* =====================================================
     * Product CRUD
     * ===================================================== */

    public Product createProduct(Product product) {
        if (product.getSku() == null) {
            product.setSku("SKU-" + System.currentTimeMillis());
        }
        product.setIsActive(true);
        return productRepository.save(product);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAllActiveProducts(pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    public Page<Product> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable);
    }

    public Page<Product> searchProductsByCategory(Long categoryId, String name, Pageable pageable) {
        return productRepository.findByCategoryIdAndNameContainingIgnoreCaseAndIsActiveTrue(
                categoryId, name, pageable);
    }

    public Page<Product> getInStockProducts(Pageable pageable) {
        return productRepository.findInStockProducts(pageable);
    }

    public Page<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max, Pageable pageable) {
        return productRepository.findByPriceBetweenAndIsActiveTrue(min, max, pageable);
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    public void updateStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    public void deactivateProduct(Long productId) {
        Product p = getProductById(productId);
        p.setIsActive(false);
        productRepository.save(p);
    }

    public void activateProduct(Long productId) {
        Product p = getProductById(productId);
        p.setIsActive(true);
        productRepository.save(p);
    }
    public Product updateProduct(Long productId, UpdateProductRequest dto) {

        Product existing = getProductById(productId);

        if (dto.getName() != null)
            existing.setName(dto.getName());

        if (dto.getDescription() != null)
            existing.setDescription(dto.getDescription());

        if (dto.getSku() != null)
            existing.setSku(dto.getSku());

        if (dto.getPrice() != null)
            existing.setPrice(dto.getPrice());

        if (dto.getStockQuantity() != null)
            existing.setStockQuantity(dto.getStockQuantity());

        if (dto.getIsActive() != null)
            existing.setIsActive(dto.getIsActive());

        /* Category Handling */
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() ->
                            new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            existing.setCategory(category);
        }

        return productRepository.save(existing);
    }

    public Product updateProduct(Long productId, Product updated) {

        Product existing = getProductById(productId);

        // Basic details
        if (updated.getName() != null)
            existing.setName(updated.getName());

        if (updated.getDescription() != null)
            existing.setDescription(updated.getDescription());

        if (updated.getSku() != null)
            existing.setSku(updated.getSku());

        if (updated.getPrice() != null)
            existing.setPrice(updated.getPrice());

        if (updated.getStockQuantity() != null)
            existing.setStockQuantity(updated.getStockQuantity());

        if (updated.getIsActive() != null)
            existing.setIsActive(updated.getIsActive());

        // Category (if provided)
        if (updated.getCategory() != null) {
            existing.setCategory(updated.getCategory());
        }

        return productRepository.save(existing);
    }
    
    public ProductDTO getProductDTOById(Long productId) {

    Product product = productRepository.findByIdWithCategory(productId)
            .orElseThrow(() ->
                    new RuntimeException("Product not found with id: " + productId));
        return ProductDTO.fromEntity(product);
    }
    @Transactional
    public void deleteProduct(Long productId) {

        Product product = getProductById(productId);

        for (ProductImage img : product.getImages()) {
            deleteFromImageKit(img.getFilePath());
        }

        productRepository.delete(product);
    }
}

