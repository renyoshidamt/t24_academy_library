package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;


/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

        
    public String isValidStatus(Integer preStatus)  {
        //RentalManage rentalManage = this.RentalManageService.findById(Long.valueOf(id));
        if(preStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RETURNED.getValue() ){    
            return "貸出ステータスは「貸出待ち」から「返却済み」に変更できません";
        }else if(preStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
            return "貸出ステータスは「貸出中」から「貸出待ち」に変更できません";
        }else if(preStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.CANCELED.getValue()){
            return "貸出ステータスは「貸出中」から「キャンセル」に変更できません";
        }else if(preStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
            return "貸出ステータスは「返却済み」から「貸出待ち」に変更できません";
        }else if(preStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENTAlING.getValue()){
            return "貸出ステータスは「返却済み」から「貸出中」に変更できません";
        }else if(preStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.CANCELED.getValue()){
            return "貸出ステータスは「返却済み」から「キャンセル」に変更できません";
        }else if(preStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
            return "貸出ステータスは「キャンセル」から「貸出待ち」に変更できません";
        }else if(preStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENTAlING.getValue()){
            return "貸出ステータスは「キャンセル」から「貸出中」に変更できません";
        }else if(preStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RETURNED.getValue()){
            return "貸出ステータスは「キャンセル」から「返却済み」に変更できません";
        }
        return null;
    }

    public String validDateCheck(){
        if(this.expectedRentalOn != null && this.expectedReturnOn != null){
            if(this.expectedReturnOn.before(this.expectedRentalOn)){
                return "返却予定日は貸出予定日より後の日付を入力してください";
            }
        }
        return null;
    }

    public String validRentalCheck(){
        if(this.expectedRentalOn != null){
            Date today = new Date();
            if(this.expectedRentalOn.before(today)){
                return "貸出予定日は現在日付以降を選択してください";
            }
        }
        return null;
    }
}
        
        
    
 

