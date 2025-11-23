package itss.demo.vietqr.vietqrsubsystem;

import lombok.Data;

@Data
public class VietQRResponse<T> {

    private boolean error;
    private String errorReason;
    private String toastMessage;
    private T object;

}
