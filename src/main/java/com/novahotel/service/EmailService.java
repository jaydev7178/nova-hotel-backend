package com.novahotel.service;

import com.novahotel.entity.Order;
import com.novahotel.entity.OrderItem;
import com.novahotel.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.List;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.owner.email}")
    private String ownerEmail;
    
    public void sendOrderConfirmationEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());
            
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("orderItems", order.getOrderItems());
            context.setVariable("user", order.getUser());
            
            String htmlContent = templateEngine.process("order-confirmation", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }
    
    public void sendOrderNotificationToOwner(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(ownerEmail);
            helper.setSubject("New Order Received - " + order.getOrderNumber());
            
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("orderItems", order.getOrderItems());
            context.setVariable("user", order.getUser());
            
            String htmlContent = templateEngine.process("order-notification-owner", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send order notification to owner", e);
        }
    }
    
    public void sendPaymentInfoEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Payment Information - " + order.getOrderNumber());
            
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("paymentInfo", order.getPaymentInfo());
            context.setVariable("user", order.getUser());
            
            String htmlContent = templateEngine.process("payment-info", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send payment info email", e);
        }
    }
    
    public void sendDeliveryInitiatedEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Delivery Initiated - " + order.getOrderNumber());
            
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("user", order.getUser());
            
            String htmlContent = templateEngine.process("delivery-initiated", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send delivery initiated email", e);
        }
    }
    
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}

