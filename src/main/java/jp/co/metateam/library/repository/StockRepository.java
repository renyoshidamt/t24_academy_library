package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);

@Query("select count(st) from Stock st"
        + " where (st.bookMst.id = ?1 and st.status = ?2)")
    int findByAvailableByBookId(Long book_id,Integer status);
   
@Query("select count(st) from Stock st"
        +" inner join RentalManage rm on rm.stock.id = st.id"
        +" where (st.bookMst.id = ?1 and rm.status in (0,1)  and rm.expectedRentalOn <= ?2 and ?2 <= rm.expectedReturnOn)")
    int findByAvailableCount(Long book_id,Date date);
}


