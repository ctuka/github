package com.tkoseli.securecapita.enumeration;

import lombok.Getter;

@Getter
public enum VerificationType {
    ACCOUNT("ACCOUNT"),
    PASSWORD("PASSWORD");

    private final String type;

    VerificationType (String type) { this.type = type; }


}
