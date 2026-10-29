package io.github.jooho2075.statrecord.game.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ResultCodeConverter implements AttributeConverter<ResultCode, String> {
    @Override
    public String convertToDatabaseColumn(ResultCode attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public ResultCode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ResultCode.fromCode(dbData);
    }
}
