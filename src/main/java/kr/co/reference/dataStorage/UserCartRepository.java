package kr.co.reference.dataStorage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCartRepository extends JpaRepository<UserCart,Integer> {
    // find by uid and prodId at userCart
    public List<UserCart> findByUidAndProdId(String uid, String prodId);
}
