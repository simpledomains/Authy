package de.reynok.authentication.core.frontend.api;


import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.backend.modules.cas.CasStatusCode;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class CasXmlResponse {
    private Document document = DocumentHelper.createDocument();
    private Element  rootElement;

    public CasXmlResponse() {
        rootElement = document.addElement("cas:serviceResponse");
        rootElement.addNamespace("cas", "http://www.yale.edu/tp/cas");
    }

    public void isFailure(CasStatusCode code, String message) {
        Element authFailure = rootElement.addElement("cas:authenticationFailure");
        authFailure.addAttribute("code", code.name().toUpperCase());
        authFailure.setText(message);
    }

    public void isSuccess(Identity identity) {
        Element successInfo = rootElement.addElement("cas:authenticationSuccess");
        Element userInfo    = successInfo.addElement("cas:user");
        userInfo.setText(identity.getUsername());
        Element attributes = successInfo.addElement("cas:attributes");

        if (!identity.getMetaData().isEmpty()) {
            for (String metaKey : identity.getMetaData().keySet()) {
                Element attr = attributes.addElement("cas:" + metaKey);
                attr.setText(identity.getMetaData().getOrDefault(metaKey, null));
            }
        }

        for (Authority authority : identity.getAuthorities()) {
            attributes.addElement("cas:groups").setText(authority.getName());
        }

        if (identity.getEmail() != null && identity.getEmail().length() > 0) {
            attributes.addElement("cas:email").setText(identity.getEmail());
        }

        if (identity.getDisplayName() != null && identity.getDisplayName().length() > 0) {
            attributes.addElement("cas:displayName").setText(identity.getDisplayName());
        }

        if (identity.getAvatar() != null && identity.getAvatar().length() > 0) {
            attributes.addElement("cas:avatar").setText(identity.getAvatar());
        }
    }

    @Override
    public String toString() {
        return document.asXML();
    }
}