package com.halaq.backend.user.service.mindee;

import com.halaq.backend.core.util.DateUtil;
import com.mindee.InferenceParameters;
import com.mindee.MindeeClientV2;
import com.mindee.input.LocalInputSource;
import com.mindee.parsing.standard.DateField;
import com.mindee.parsing.v2.InferenceResponse;
import com.mindee.parsing.v2.field.InferenceFields;
import com.mindee.parsing.v2.field.ListField;
import com.mindee.parsing.v2.field.SimpleField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MindeeService {

    private final MindeeClientV2 mindeeClient;

    private static final String DOCTI_MODEL_ID = "9942e28f-ef7e-4cfc-96ed-18fcaf3ebb8f";

    public MindeeService(@Value("${app.mindee.api-key}") String apiKey) {
        this.mindeeClient = new MindeeClientV2(apiKey);
    }

    /**
     * Process International ID document and extract information
     *
     * @param file The uploaded document file
     * @return Extracted document information
     * @throws IOException if file processing fails
     * @throws InterruptedException if processing is interrupted
     */
    public DocumentInfo processInternationalId(MultipartFile file) throws IOException, InterruptedException {
        try {
            // Create input source from uploaded file
            LocalInputSource inputSource = new LocalInputSource(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            InferenceParameters options = InferenceParameters
                    .builder(DOCTI_MODEL_ID)
                    .build();

            // Parse the document asynchronously
            InferenceResponse response = mindeeClient.enqueueAndGetInference(
                    inputSource,
                    options
            );
            // Extract document information
            return mapToDocumentInfo(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    /**
     * Process International ID from file path
     *
     * @param filePath Path to the document file
     * @return Extracted document information
     * @throws IOException if file processing fails
     * @throws InterruptedException if processing is interrupted
     */
    public DocumentInfo processInternationalId(String filePath) throws IOException, InterruptedException {
        try {
            LocalInputSource inputSource = new LocalInputSource(new File(filePath));
            InferenceParameters options = InferenceParameters
                    .builder(DOCTI_MODEL_ID)
                    .build();

            InferenceResponse  response = mindeeClient.enqueueAndGetInference(
                    inputSource,
                    options
            );

            return mapToDocumentInfo(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    /**
     * Get raw parsing response for advanced processing
     */
    public InferenceResponse getRawResponse(MultipartFile file) throws IOException, InterruptedException {
        LocalInputSource inputSource = new LocalInputSource(
                file.getInputStream(),
                file.getOriginalFilename()
        );
        InferenceParameters options = InferenceParameters
                .builder(DOCTI_MODEL_ID)
                .build();
        return mindeeClient.enqueueAndGetInference(inputSource, options);
    }

    /**
     * Validate document quality and readability
     */
    public DocumentValidation validateDocument(MultipartFile file) throws IOException, InterruptedException {
        DocumentInfo info = processInternationalId(file);

        DocumentValidation validation = new DocumentValidation();
        validation.setValid(true);
        validation.setConfidence(calculateOverallConfidence(info));

        // Check for missing critical fields
        if (info.getDocumentNumber() == null || info.getDocumentNumber().isEmpty()) {
            validation.addWarning("Document number not detected");
        }

        if (info.getGivenNames() == null || info.getGivenNames().isEmpty()) {
            validation.addWarning("Given names not clearly detected");
        }

        if (info.getSurnames() == null || info.getSurnames().isEmpty()) {
            validation.addWarning("Surnames not detected");
        }

        if (info.getBirthDate() == null) {
            validation.addWarning("Birth date not detected");
        }

        if (info.getExpiryDate() == null) {
            validation.addWarning("Expiry date not detected");
        }

        // Set validity based on warnings
        if (validation.getWarnings().size() > 2) {
            validation.setValid(false);
        }

        return validation;
    }

    /**
     * Map Mindee document to our custom DocumentInfo object
     */
    private DocumentInfo mapToDocumentInfo(InferenceResponse response) {
        DocumentInfo info = new DocumentInfo();

        try {
            // This is the container for all extracted fields
            InferenceFields fields = response.getInference().getResult().getFields();

            //--- Map String Fields ---
            SimpleField docNumField = fields.getSimpleField("document_number");
            if (docNumField != null) {
                info.setDocumentNumber(docNumField.getStringValue());
            }

            SimpleField birthPlaceField = fields.getSimpleField("place_of_birth");
            if (birthPlaceField != null) {
                info.setBirthPlace(birthPlaceField.getStringValue());
            }

            SimpleField countryField = fields.getSimpleField("authority");
            if (countryField != null) {
                info.setCountryOfIssue(countryField.getStringValue());
            }

            // ... add other simple string fields here following the same pattern ...
            SimpleField sexField = fields.getSimpleField("sex");
            if (sexField != null) {
                info.setSex(sexField.getStringValue());
            }

            SimpleField nationalityField = fields.getSimpleField("nationality");
            if (nationalityField != null) {
                info.setNationality(nationalityField.getStringValue());
            }

            //--- Map Date Fields ---
            SimpleField birthDateField = fields.getSimpleField("date_of_birth");
            if (birthDateField != null && birthDateField.getStringValue() != null) {
                info.setBirthDate(LocalDate.parse(birthDateField.getStringValue()));
            }

            SimpleField expiryDateField = fields.getSimpleField("date_of_expiry");
            if (expiryDateField != null && expiryDateField.getStringValue() != null) {
                info.setExpiryDate(LocalDate.parse(expiryDateField.getStringValue()));
            }


            SimpleField issueDateField = fields.getSimpleField("date_of_issue");
            if (issueDateField != null && issueDateField.getStringValue() != null) {
                info.setIssueDate(LocalDate.parse(issueDateField.getStringValue()));
            }
            //--- Map List Fields (like given names or surnames) ---
            SimpleField givenNamesField = fields.getSimpleField("given_names");
            if (givenNamesField != null && givenNamesField.getStringValue() != null) {
                List<String> givenNames = List.of(givenNamesField.getStringValue());
                info.setGivenNames(givenNames);
            }

            SimpleField surnamesField = fields.getSimpleField("surnames");
            if (surnamesField != null && surnamesField.getStringValue()!=null) {
                List<String> surnames = List.of(surnamesField.getStringValue());
                info.setSurnames(surnames);
            }

            // Post processing
            info.setExpired(isDocumentExpired(info.getExpiryDate()));
            info.setOverallConfidence(calculateOverallConfidence(info));

        } catch (Exception e) {
            // It's good practice to log the error
            // log.error("Error mapping response to DocumentInfo", e);
            throw new RuntimeException("Error mapping response to DocumentInfo: " + e.getMessage(), e);
        }

        return info;
    }

    private boolean isDocumentExpired(LocalDate expiryDate) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    private double calculateOverallConfidence(DocumentInfo info) {
        // Simple confidence calculation based on filled fields
        int totalFields = 12;
        int filledFields = 0;

        if (info.getDocumentNumber() != null && !info.getDocumentNumber().isEmpty()) filledFields++;
        if (info.getGivenNames() != null && !info.getGivenNames().isEmpty()) filledFields++;
        if (info.getSurnames() != null && !info.getSurnames().isEmpty()) filledFields++;
        if (info.getBirthDate() != null) filledFields++;
        if (info.getExpiryDate() != null) filledFields++;
        if (info.getBirthPlace() != null && !info.getBirthPlace().isEmpty()) filledFields++;
        if (info.getCountryOfIssue() != null && !info.getCountryOfIssue().isEmpty()) filledFields++;
        if (info.getSex() != null && !info.getSex().isEmpty()) filledFields++;
        if (info.getNationality() != null && !info.getNationality().isEmpty()) filledFields++;
        if (info.getDocumentType() != null && !info.getDocumentType().isEmpty()) filledFields++;
        if (info.getPersonalNumber() != null && !info.getPersonalNumber().isEmpty()) filledFields++;
        if (info.getAddress() != null && !info.getAddress().isEmpty()) filledFields++;

        return (double) filledFields / totalFields;
    }

    /**
     * DocumentInfo class to hold extracted information
     */
    public static class DocumentInfo {
        private String documentNumber;
        private List<String> givenNames = new ArrayList<>();
        private List<String> surnames = new ArrayList<>();
        private LocalDate birthDate;
        private LocalDate expiryDate;
        private LocalDate issueDate;
        private String birthPlace;
        private String countryOfIssue;
        private String stateOfIssue;
        private String address;
        private String sex;
        private String nationality;
        private String personalNumber;
        private String documentType;
        private String mrzLine1;
        private String mrzLine2;
        private String mrzLine3;
        private boolean expired;
        private double overallConfidence;

        // Getters and Setters
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

        public List<String> getGivenNames() { return givenNames; }
        public void setGivenNames(List<String> givenNames) { this.givenNames = givenNames; }

        public List<String> getSurnames() { return surnames; }
        public void setSurnames(List<String> surnames) { this.surnames = surnames; }

        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

        public LocalDate getIssueDate() { return issueDate; }
        public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

        public String getBirthPlace() { return birthPlace; }
        public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }

        public String getCountryOfIssue() { return countryOfIssue; }
        public void setCountryOfIssue(String countryOfIssue) { this.countryOfIssue = countryOfIssue; }

        public String getStateOfIssue() { return stateOfIssue; }
        public void setStateOfIssue(String stateOfIssue) { this.stateOfIssue = stateOfIssue; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getSex() { return sex; }
        public void setSex(String sex) { this.sex = sex; }

        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }

        public String getPersonalNumber() { return personalNumber; }
        public void setPersonalNumber(String personalNumber) { this.personalNumber = personalNumber; }

        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }

        public String getMrzLine1() { return mrzLine1; }
        public void setMrzLine1(String mrzLine1) { this.mrzLine1 = mrzLine1; }

        public String getMrzLine2() { return mrzLine2; }
        public void setMrzLine2(String mrzLine2) { this.mrzLine2 = mrzLine2; }

        public String getMrzLine3() { return mrzLine3; }
        public void setMrzLine3(String mrzLine3) { this.mrzLine3 = mrzLine3; }

        public boolean isExpired() { return expired; }
        public void setExpired(boolean expired) { this.expired = expired; }

        public double getOverallConfidence() { return overallConfidence; }
        public void setOverallConfidence(double overallConfidence) { this.overallConfidence = overallConfidence; }

        // Helper methods
        public String getFullName() {
            List<String> allNames = new ArrayList<>();
            if (givenNames != null) allNames.addAll(givenNames);
            if (surnames != null) allNames.addAll(surnames);
            return String.join(" ", allNames);
        }

        public String getGivenNamesAsString() {
            return givenNames != null ? String.join(" ", givenNames) : "";
        }

        public String getSurnamesAsString() {
            return surnames != null ? String.join(" ", surnames) : "";
        }

        @Override
        public String toString() {
            return "DocumentInfo{" +
                    "documentNumber='" + documentNumber + '\'' +
                    ", givenNames=" + givenNames +
                    ", surnames=" + surnames +
                    ", birthDate=" + birthDate +
                    ", expiryDate=" + expiryDate +
                    ", countryOfIssue='" + countryOfIssue + '\'' +
                    ", documentType='" + documentType + '\'' +
                    ", expired=" + expired +
                    ", confidence=" + String.format("%.2f", overallConfidence) +
                    '}';
        }
    }

    /**
     * DocumentValidation class for validation results
     */
    public static class DocumentValidation {
        private boolean valid;
        private double confidence;
        private List<String> warnings = new ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }

        public void addWarning(String warning) { this.warnings.add(warning); }

        @Override
        public String toString() {
            return "DocumentValidation{" +
                    "valid=" + valid +
                    ", confidence=" + String.format("%.2f", confidence) +
                    ", warnings=" + warnings.size() +
                    '}';
        }
    }
}