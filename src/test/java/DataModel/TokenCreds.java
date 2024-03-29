package DataModel;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenCreds {
    private String username;
    private String password;

}
