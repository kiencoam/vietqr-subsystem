package itss.demo.vietqr.vietqrsubsystem;

import lombok.Data;

@Data
class VietQRToken {

    private String accessToken;
    private String tokenType;
    private int expiresIn;

}
