package com.halaq.backend.service.service.impl;

import com.halaq.backend.service.entity.Review;
import com.halaq.backend.service.criteria.ReviewCriteria;
import com.halaq.backend.service.repository.ReviewRepository;
import com.halaq.backend.service.service.facade.ReviewService;
import com.halaq.backend.service.specification.ReviewSpecification;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.service.facade.BarberService;
import com.halaq.backend.core.exception.BusinessRuleException;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl extends AbstractServiceImpl<Review, ReviewCriteria, ReviewRepository> implements ReviewService {

    @Autowired
    private BarberService barberService;

    public ReviewServiceImpl(ReviewRepository dao) {
        super(dao);
    }

    @Override
    @Transactional
    public Review create(Review review) {
        // Business logic: Validate rating and that the booking is completed
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessRuleException("Rating must be between 1 and 5.");
        }
        // TODO: Add logic to verify that the booking was completed by the author

        Review savedReview = super.create(review);

        // After saving, update the barber's average rating
        updateBarberAverageRating(review.getBarber().getId());

        return savedReview;
    }

    private void updateBarberAverageRating(Long barberId) {
        Barber barber = barberService.findById(barberId);
        if (barber != null) {
            ReviewCriteria criteria = new ReviewCriteria();
            criteria.setBarberId(barberId);
            long reviewCount = dao.count(new ReviewSpecification(criteria));
            // You might need a custom repository method for sum
            // double totalRating = dao.sumRatingByBarberId(barberId);
            // For now, let's just refetch all reviews to calculate
            double totalRating = findByCriteria(criteria).stream().mapToInt(Review::getRating).sum();

            if (reviewCount > 0) {
                barber.setAverageRating(totalRating / reviewCount);
                barberService.update(barber);
            }
        }
    }

    @Override
    public void configure() {
        super.configure(Review.class, ReviewSpecification.class);
    }
}