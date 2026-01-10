package com.halaq.backend.service.controller;

import com.halaq.backend.service.converter.ReviewConverter;
import com.halaq.backend.service.criteria.ReviewCriteria;
import com.halaq.backend.service.dto.ReviewDto;
import com.halaq.backend.service.entity.Review;
import com.halaq.backend.service.service.facade.ReviewService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reviews")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController extends AbstractController<Review, ReviewDto, ReviewCriteria, ReviewService, ReviewConverter> {

    public ReviewController(ReviewService service, ReviewConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Submit a new review for a booking")
    @PostMapping("/")
    public ResponseEntity<ReviewDto> save(@RequestBody ReviewDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Finds reviews by criteria (e.g., for a specific barber)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<ReviewDto>> findByCriteria(@RequestBody ReviewCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }
}