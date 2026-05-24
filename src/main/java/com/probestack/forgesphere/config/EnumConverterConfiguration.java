package com.probestack.forgesphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleSeverity;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;
import com.probestack.forgesphere.model.ScanMode;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.model.ScanStatus;
import com.probestack.forgesphere.model.SourceType;

@Configuration
public class EnumConverterConfiguration {

    @Bean(name = "com.probestack.forgestudio.config.EnumConverterConfiguration.assetTypeConverter")
    Converter<String, AssetType> assetTypeConverter() {
        return new Converter<String, AssetType>() {
            @Override
            public AssetType convert(String source) {
                return AssetType.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.complianceStatusConverter")
    Converter<String, ComplianceStatus> complianceStatusConverter() {
        return new Converter<String, ComplianceStatus>() {
            @Override
            public ComplianceStatus convert(String source) {
                return ComplianceStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.ruleCategoryConverter")
    Converter<String, RuleCategory> ruleCategoryConverter() {
        return new Converter<String, RuleCategory>() {
            @Override
            public RuleCategory convert(String source) {
                return RuleCategory.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.ruleSeverityConverter")
    Converter<String, RuleSeverity> ruleSeverityConverter() {
        return new Converter<String, RuleSeverity>() {
            @Override
            public RuleSeverity convert(String source) {
                return RuleSeverity.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.ruleStatusConverter")
    Converter<String, RuleStatus> ruleStatusConverter() {
        return new Converter<String, RuleStatus>() {
            @Override
            public RuleStatus convert(String source) {
                return RuleStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.ruleTypeConverter")
    Converter<String, RuleType> ruleTypeConverter() {
        return new Converter<String, RuleType>() {
            @Override
            public RuleType convert(String source) {
                return RuleType.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.scanModeConverter")
    Converter<String, ScanMode> scanModeConverter() {
        return new Converter<String, ScanMode>() {
            @Override
            public ScanMode convert(String source) {
                return ScanMode.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.scanResultStatusConverter")
    Converter<String, ScanResultStatus> scanResultStatusConverter() {
        return new Converter<String, ScanResultStatus>() {
            @Override
            public ScanResultStatus convert(String source) {
                return ScanResultStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.scanStatusConverter")
    Converter<String, ScanStatus> scanStatusConverter() {
        return new Converter<String, ScanStatus>() {
            @Override
            public ScanStatus convert(String source) {
                return ScanStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "com.probestack.forgesphere.config.EnumConverterConfiguration.sourceTypeConverter")
    Converter<String, SourceType> sourceTypeConverter() {
        return new Converter<String, SourceType>() {
            @Override
            public SourceType convert(String source) {
                return SourceType.fromValue(source);
            }
        };
    }

}
