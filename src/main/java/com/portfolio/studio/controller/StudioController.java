package com.portfolio.studio.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.portfolio.studio.model.BlogPost;
import com.portfolio.studio.model.GalleryEntry;
import com.portfolio.studio.model.Project;
import com.portfolio.studio.service.PortfolioService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudioController {

    private final PortfolioService portfolioService;

    public StudioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/cmsmgmnt/sign-in")
    public String signIn(Authentication authentication, Model model, HttpSession session) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/cmsmgmnt/dashboard";
        }

        model.addAttribute("pageTitle", "Sign in");
        model.addAttribute("authError", session.getAttribute("studioAuthError"));
        session.removeAttribute("studioAuthError");
        return "studio/auth/login";
    }

    @GetMapping("/cmsmgmnt/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        applyStudioShell(model, "dashboard", authentication);
        model.addAttribute("counts", portfolioService.loadDashboardCounts());
        model.addAttribute("draftProjects", portfolioService.listDraftProjects());
        model.addAttribute("draftGallery", portfolioService.listDraftGalleryEntries());
        model.addAttribute("draftPosts", portfolioService.listDraftBlogPosts());
        model.addAttribute("recentAudit", portfolioService.listRecentAuditEntries());
        return "studio/dashboard/index";
    }

    @GetMapping("/cmsmgmnt/projects")
    public String projects(Model model, Authentication authentication) {
        applyStudioShell(model, "projects", authentication);
        model.addAttribute("projects", portfolioService.listAllProjects());
        return "studio/projects/list";
    }

    @GetMapping("/cmsmgmnt/projects/new")
    public String newProject(Model model, Authentication authentication) {
        applyStudioShell(model, "projects", authentication);
        model.addAttribute("project", new Project());
        return "studio/projects/form";
    }

    @GetMapping("/cmsmgmnt/projects/{id}/edit")
    public String editProject(@PathVariable long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        return portfolioService.findProjectById(id)
            .map(project -> {
                applyStudioShell(model, "projects", authentication);
                model.addAttribute("project", project);
                return "studio/projects/form";
            })
            .orElseGet(() -> {
                flashError(redirectAttributes, "Project not found.");
                return "redirect:/cmsmgmnt/projects";
            });
    }

    @PostMapping("/cmsmgmnt/projects/save")
    public String saveProject(
        @Valid @ModelAttribute("project") Project project,
        BindingResult bindingResult,
        @RequestParam(name = "cardImageFile", required = false) MultipartFile cardImageFile,
        @RequestParam(name = "galleryFiles", required = false) MultipartFile[] galleryFiles,
        Model model,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            applyStudioShell(model, "projects", authentication);
            model.addAttribute("formError", "Please complete the required fields.");
            return "studio/projects/form";
        }

        try {
            portfolioService.saveProject(project, cardImageFile, galleryFiles);
            flashSaved(redirectAttributes, "Project saved.");
            return "redirect:/cmsmgmnt/projects";
        } catch (RuntimeException exception) {
            applyStudioShell(model, "projects", authentication);
            model.addAttribute("formError", exception.getMessage());
            return "studio/projects/form";
        }
    }

    @PostMapping("/cmsmgmnt/projects/{id}/delete")
    public String deleteProject(@PathVariable long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteProject(id);
        flashDeleted(redirectAttributes, "Project deleted.");
        return "redirect:/cmsmgmnt/projects";
    }

    @GetMapping("/cmsmgmnt/gallery")
    public String galleryEntries(Model model, Authentication authentication) {
        applyStudioShell(model, "gallery", authentication);
        model.addAttribute("entries", portfolioService.listAllGalleryEntries());
        return "studio/gallery/list";
    }

    @GetMapping("/cmsmgmnt/gallery/new")
    public String newGalleryEntry(Model model, Authentication authentication) {
        applyStudioShell(model, "gallery", authentication);
        model.addAttribute("entry", new GalleryEntry());
        return "studio/gallery/form";
    }

    @GetMapping("/cmsmgmnt/gallery/{id}/edit")
    public String editGalleryEntry(@PathVariable long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        return portfolioService.findGalleryEntryById(id)
            .map(entry -> {
                applyStudioShell(model, "gallery", authentication);
                model.addAttribute("entry", entry);
                return "studio/gallery/form";
            })
            .orElseGet(() -> {
                flashError(redirectAttributes, "Gallery entry not found.");
                return "redirect:/cmsmgmnt/gallery";
            });
    }

    @PostMapping("/cmsmgmnt/gallery/save")
    public String saveGalleryEntry(
        @Valid @ModelAttribute("entry") GalleryEntry entry,
        BindingResult bindingResult,
        @RequestParam(name = "mediaFiles", required = false) MultipartFile[] mediaFiles,
        Model model,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            applyStudioShell(model, "gallery", authentication);
            model.addAttribute("formError", "Please complete the required fields.");
            return "studio/gallery/form";
        }

        try {
            portfolioService.saveGalleryEntry(entry, mediaFiles);
            flashSaved(redirectAttributes, "Gallery entry saved.");
            return "redirect:/cmsmgmnt/gallery";
        } catch (RuntimeException exception) {
            applyStudioShell(model, "gallery", authentication);
            model.addAttribute("formError", exception.getMessage());
            return "studio/gallery/form";
        }
    }

    @PostMapping("/cmsmgmnt/gallery/{id}/delete")
    public String deleteGalleryEntry(@PathVariable long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteGalleryEntry(id);
        flashDeleted(redirectAttributes, "Gallery entry deleted.");
        return "redirect:/cmsmgmnt/gallery";
    }

    @GetMapping("/cmsmgmnt/blog")
    public String blogPosts(Model model, Authentication authentication) {
        applyStudioShell(model, "blog", authentication);
        model.addAttribute("posts", portfolioService.listAllBlogPosts());
        return "studio/blog/list";
    }

    @GetMapping("/cmsmgmnt/blog/new")
    public String newBlogPost(Model model, Authentication authentication) {
        applyStudioShell(model, "blog", authentication);
        model.addAttribute("post", new BlogPost());
        return "studio/blog/form";
    }

    @GetMapping("/cmsmgmnt/blog/{id}/edit")
    public String editBlogPost(@PathVariable long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        return portfolioService.findBlogPostById(id)
            .map(post -> {
                applyStudioShell(model, "blog", authentication);
                model.addAttribute("post", post);
                return "studio/blog/form";
            })
            .orElseGet(() -> {
                flashError(redirectAttributes, "Blog post not found.");
                return "redirect:/cmsmgmnt/blog";
            });
    }

    @PostMapping("/cmsmgmnt/blog/save")
    public String saveBlogPost(
        @Valid @ModelAttribute("post") BlogPost post,
        BindingResult bindingResult,
        @RequestParam(name = "coverImageFile", required = false) MultipartFile coverImageFile,
        Model model,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            applyStudioShell(model, "blog", authentication);
            model.addAttribute("formError", "Please complete the required fields.");
            return "studio/blog/form";
        }

        try {
            portfolioService.saveBlogPost(post, coverImageFile);
            flashSaved(redirectAttributes, "Blog post saved.");
            return "redirect:/cmsmgmnt/blog";
        } catch (RuntimeException exception) {
            applyStudioShell(model, "blog", authentication);
            model.addAttribute("formError", exception.getMessage());
            return "studio/blog/form";
        }
    }

    @PostMapping("/cmsmgmnt/blog/{id}/delete")
    public String deleteBlogPost(@PathVariable long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteBlogPost(id);
        flashDeleted(redirectAttributes, "Blog post deleted.");
        return "redirect:/cmsmgmnt/blog";
    }

    @GetMapping("/cmsmgmnt/media")
    public String mediaLibrary(Model model, Authentication authentication) {
        applyStudioShell(model, "media", authentication);
        model.addAttribute("mediaItems", portfolioService.listMediaLibrary());
        return "studio/media/index";
    }

    private void applyStudioShell(Model model, String activeNav, Authentication authentication) {
        String place = switch (activeNav) {
            case "dashboard" -> "Dashboard";
            case "projects" -> "Projects";
            case "gallery" -> "Gallery";
            case "blog" -> "Blog";
            case "media" -> "Media";
            default -> "Desk";
        };
        model.addAttribute("pageTitle", place);
        model.addAttribute("activeNav", activeNav);
        model.addAttribute("cmsPlace", place);
        model.addAttribute("cmsLocation", "You are now at: " + place);
        model.addAttribute("studioUsername", authentication == null ? "" : authentication.getName());
    }

    private void flashSaved(RedirectAttributes redirectAttributes, String message) {
        flash(redirectAttributes, "saved", message);
    }

    private void flashDeleted(RedirectAttributes redirectAttributes, String message) {
        flash(redirectAttributes, "deleted", message);
    }

    private void flashError(RedirectAttributes redirectAttributes, String message) {
        flash(redirectAttributes, "error", message);
    }

    private void flash(RedirectAttributes redirectAttributes, String kind, String message) {
        redirectAttributes.addFlashAttribute("flashMessage", message);
        redirectAttributes.addFlashAttribute("flashKind", kind);
    }
}
