package de.reynok.authentication.core.security.cas;


import de.reynok.authentication.core.database.entity.Authority;
import de.reynok.authentication.core.database.entity.Identity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultText;

public class ValidateResponse {
    private Document document = DocumentHelper.createDocument();
    private Element rootElement;

    public enum StatusCode {
        INVALID_TICKET,
        USER_REVOKED,
        MISSING_SERVICE,
        DENIED,
    }

    public ValidateResponse() {
        rootElement = document.addElement("cas:serviceResponse");
        rootElement.addNamespace("cas", "http://www.yale.edu/tp/cas");
    }

    public void isFailure(StatusCode code, String message){
        Element authFailure = rootElement.addElement("cas:authenticationFailure");
        authFailure.addAttribute("code", code.name().toUpperCase());
        authFailure.setText(message);
    }

    public void isSuccess(Identity identity) {
        Element successInfo = rootElement.addElement("cas:authenticationSuccess");
        Element userInfo = successInfo.addElement("cas:user");
        userInfo.setText(identity.getUsername());
        Element attributes = successInfo.addElement("cas:attributes");

        if (!identity.getMetaData().isEmpty()) {
            for(String metaKey: identity.getMetaData().keySet()) {
                Element attr = attributes.addElement("cas:" + metaKey);
                attr.setText(identity.getMetaData().getOrDefault(metaKey, null));
            }
        }

        for (Authority authority: identity.getAuthorities()) {
            attributes.addElement("cas:groups").setText(authority.getName());
        }
    }

    @Override
    public String toString() {
        return document.asXML();
    }
}