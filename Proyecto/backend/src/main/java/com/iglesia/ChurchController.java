package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/church")
public class ChurchController {

    private final ChurchService churchService;

    public ChurchController(ChurchService churchService) {
        this.churchService = churchService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ChurchResponse create(@RequestBody ChurchRequest request) {
        return churchService.create(request);
    }

    @GetMapping
    public ChurchResponse get() {
        return churchService.get();
    }

    public record ChurchRequest(
        @NotBlank String name,
        String address
    ) {}

    public record ChurchResponse(
        Long id,
        String name,
        String address
    ) {
        public static ChurchResponse from(Church church) {
            return new ChurchResponse(church.getId(), church.getName(), church.getAddress());
        }
    }
}