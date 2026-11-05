package com.bookloop.repository;

import com.bookloop.entity.SellerPaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerPaymentAccountRepository extends JpaRepository<SellerPaymentAccount, Long> {

    @Query("SELECT a FROM SellerPaymentAccount a WHERE a.seller.id = :sellerId AND a.providerCode = :providerCode ORDER BY a.createdAt DESC")
    List<SellerPaymentAccount> findAllBySellerIdAndProviderCode(@Param("sellerId") Long sellerId, @Param("providerCode") String providerCode);

    default Optional<SellerPaymentAccount> findBySellerIdAndProviderCode(Long sellerId, String providerCode) {
        List<SellerPaymentAccount> accounts = findAllBySellerIdAndProviderCode(sellerId, providerCode);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }

    @Query("SELECT a FROM SellerPaymentAccount a WHERE a.seller.id = :sellerId ORDER BY a.createdAt DESC")
    List<SellerPaymentAccount> findAllBySellerId(@Param("sellerId") Long sellerId);

    default Optional<SellerPaymentAccount> findBySellerId(Long sellerId) {
        List<SellerPaymentAccount> accounts = findAllBySellerId(sellerId);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }
}
