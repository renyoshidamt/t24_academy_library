package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,RentalManageRepository rentalManageRepository){
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public int findByAvailableByBookId(Long book_id){
        int availableStockCount = this.stockRepository.findByAvailableByBookId(book_id,Constants.STOCK_AVAILABLE);
        int unAvailableStockCount = this.stockRepository.findByAvailableByBookId(book_id,Constants.STOCK_UNAVAILABLE);//書籍IDをもとに利用可能な在庫数を持ってきている
        Integer stockCount = availableStockCount + unAvailableStockCount;
        return stockCount;

    }

    @Transactional
    public int findByAvailableCount(Long book_id,Date date){
        int rentalAvailableCount = this.stockRepository.findByAvailableCount(book_id,date);
        return rentalAvailableCount;
    }

    @Transactional
    public List<String> findByAvailableStockId(Long bookId,Date date){
        return this.rentalManageRepository.findByAvailableStockId(bookId,date);
    }


    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }
        return daysOfWeek;
    }

    public String[][] generateValues(Integer year, Integer month, Integer daysInMonth) {
        /* ランダムではなく書籍ごとの処理に変換
        　　１・在庫テーブルから書籍をすべて取得
        　　２・在庫数の項目にセット
        　　３・書籍タイトルの項目にセット

        　　４・在庫テーブルから利用可能な書籍をすべて取得
                貸出ステータス0,1は減算
                貸出テーブルの在庫管理番号をキーに在庫取ってくる
                ステータス紐づいている本と在庫数が必要（all在庫数）
        　　５・貸出登録画面に遷移
        　　６・プレースホルダーにセット*/  
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month-1, daysInMonth,0,0,0);
        Date date = calendar.getTime();
        List<BookMst> bookList = bookMstRepository.findAll();//書籍情報全件取得
        int bookNum = bookList.size();

        //配列「在庫分」「日付＋3（書籍名、在庫数、在庫管理番号)」
        String [][] bookCalender = new String[bookNum][daysInMonth+3]; 

       for (int i = 0; i<bookNum; i++) {
            BookMst book = bookList.get(i);
            String bookTitle = book.getTitle();
            List<String> availableStockId = findByAvailableStockId(book.getId(),date);
            int stockCount = findByAvailableByBookId(book.getId());//書籍IDをもとに在庫数

            bookCalender[i][0] = bookTitle;
            bookCalender[i][1] = String.valueOf(stockCount);

            if(!availableStockId.isEmpty()){
                bookCalender[i][2] = availableStockId.get(i);
                bookCalender[0][2] = availableStockId.get(5);
            }

            for(int j = 3; j<daysInMonth+3; j++){
                calendar.set(year, month-1, j-2,0,0,0);
                Date day = calendar.getTime();
                int rentalAvailableCount = findByAvailableCount(book.getId(),day);
                int unAvailableStockCount = this.stockRepository.findByAvailableByBookId(book.getId(),Constants.STOCK_UNAVAILABLE);
                int dailyAvailableCount = stockCount - rentalAvailableCount-unAvailableStockCount;
                bookCalender[i][j] = String.valueOf(dailyAvailableCount);
            }
        }
        return bookCalender;
    }
}
