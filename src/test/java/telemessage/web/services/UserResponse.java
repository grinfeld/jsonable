package telemessage.web.services;

import com.mikerusoft.jsonable.annotations.JsonClass;
import com.mikerusoft.jsonable.annotations.JsonField;

import java.util.List;

@JsonClass
public class UserResponse {
    @JsonField protected int resultCode;
    @JsonField protected String resultDescription;
    @JsonField protected List<MultiValueUserField> userFields;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public List<MultiValueUserField> getUserFields() {
        return userFields;
    }

    public void setUserFields(List<MultiValueUserField> userFields) {
        this.userFields = userFields;
    }
}
