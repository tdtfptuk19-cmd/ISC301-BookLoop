package com.bookloop.service;

import com.bookloop.entity.Order;
import com.bookloop.entity.OrderStatus;
import com.bookloop.entity.Review;
import com.bookloop.entity.User;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.OrderRepository;
import com.bookloop.repository.ReviewRepository;
import com.bookloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing buyer reviews and ratings for completed transactions.
 */
@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderRepository orderRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submits a rating and review for a seller on a COMPLETED order.
     * Enforces rules:
     * 1. Only the buyer of the order can review.
     * 2. The order must be in COMPLETED status.
     * 3. The seller must be an actual seller of items in the order.
     * 4. Rating must be 1 to 5.
     * 5. Prevents duplicate reviews for the same order and seller.
     */
    public Review submitReview(Long orderId, Long sellerId, int rating, String comment, User buyer) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // Rule 1: Buyer authentication check
        if (!order.isBuyer(buyer.getId())) {
            throw new UnauthorizedOperationException("Only the buyer of this order can submit a review.");
        }

        // Rule 2: Order must be COMPLETED
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Reviews can only be submitted for COMPLETED orders (current status: "
                    + order.getStatus().getDisplayName() + ").");
        }

        // Rule 3: Seller must have sold items in this order
        if (!order.hasSeller(sellerId)) {
            throw new IllegalArgumentException("The specified user is not a seller in this order.");
        }

        // Rule 4: Rating validation
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }

        // Rule 5: Duplicate check
        if (reviewRepository.existsByOrderIdAndSellerId(orderId, sellerId)) {
            throw new IllegalArgumentException("You have already submitted a review for this order and seller.");
        }

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with ID: " + sellerId));

        Review review = new Review(order, buyer, seller, rating, comment);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForSeller(Long sellerId) {
        return reviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForSeller(Long sellerId) {
        Double avg = reviewRepository.findAverageRatingBySellerId(sellerId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Transactional(readOnly = true)
    public long getReviewCountForSeller(Long sellerId) {
        return reviewRepository.countBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(Long orderId, Long sellerId) {
        return reviewRepository.existsByOrderIdAndSellerId(orderId, sellerId);
    }
}
