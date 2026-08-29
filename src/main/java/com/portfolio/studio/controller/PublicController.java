package com.portfolio.studio.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.studio.service.PortfolioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicController {

    private final PortfolioService portfolioService;
    private final ObjectMapper objectMapper;

    public PublicController(PortfolioService portfolioService, ObjectMapper objectMapper) {
        this.portfolioService = portfolioService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String home(Model model) throws JsonProcessingException {
        model.addAttribute("pageTitle", "Portfolio");
        model.addAttribute("workProjectsJson", objectMapper.writeValueAsString(portfolioService.buildWorkProjectsMap()));
        model.addAttribute("galleryEntries", portfolioService.listPublishedGalleryEntries());
        model.addAttribute("blogPosts", portfolioService.listPublishedBlogPosts());
        return "public/index";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("pageTitle", "Gallery");
        model.addAttribute("galleryEntries", portfolioService.listPublishedGalleryEntries());
        return "public/gallery";
    }

    @GetMapping("/gallery/{id}")
    public String galleryDetail(@PathVariable long id, Model model) {
        return portfolioService.findPublishedGalleryEntryById(id)
            .map(entry -> {
                model.addAttribute("pageTitle", entry.getTitle());
                model.addAttribute("entry", entry);
                return "public/gallery-detail";
            })
            .orElse("redirect:/gallery");
    }

    @GetMapping("/work/{id}")
    public String workDetail(@PathVariable long id, Model model) {
        return portfolioService.findPublishedProjectById(id)
            .map(project -> {
                model.addAttribute("pageTitle", project.getTitle());
                model.addAttribute("project", project);
                return "public/work-detail";
            })
            .orElse("redirect:/");
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        model.addAttribute("pageTitle", "Blog");
        model.addAttribute("blogPosts", portfolioService.listPublishedBlogPosts());
        return "public/blog";
    }

    @GetMapping("/blog/{id}")
    public String blogDetail(@PathVariable long id, Model model) {
        return portfolioService.findPublishedBlogPostById(id)
            .map(post -> {
                model.addAttribute("pageTitle", post.getTitle());
                model.addAttribute("post", post);
                return "public/blog-detail";
            })
            .orElse("redirect:/blog");
    }
}
