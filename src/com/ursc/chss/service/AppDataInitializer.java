package com.ursc.chss.service;

import com.ursc.chss.dao.RejectionReasonDAO;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Seeds the {@code rejection_reasons} MySQL table on first startup.
 *
 * <p>Reads {@code Rejections.json} from the classpath (copy it into the
 * Sandesh {@code src/} folder so it lands in {@code WEB-INF/classes/}). If the
 * file is missing or malformed, the built-in defaults below are used so the
 * module always works. The table is only populated when it is empty, so the
 * file is a one-time bootstrap, not a per-request load.
 */
public final class AppDataInitializer {

    /** Default 18 rejection reasons - identical to the shipped Rejections.json. */
    private static final Map<Integer, String> DEFAULT_REASONS = new LinkedHashMap<>();
    static {
        DEFAULT_REASONS.put(1, "The claim has not been submitted within the prescribed time limit, i.e., three months from the date of consultation/prescription. Hence approval from Administration is required.");
        DEFAULT_REASONS.put(2, "Medicines purchased are inadmissible under CHSS/CSMA rules.");
        DEFAULT_REASONS.put(3, "The Medical claim/s is/are not filled properly by the employee.");
        DEFAULT_REASONS.put(4, "The prescription is not signed/sealed by AMO.");
        DEFAULT_REASONS.put(5, "Kindly fill the attached Declaration Form and forward to CHSS-Accounts along with the claim for processing.");
        DEFAULT_REASONS.put(6, "The date of the cash bill is prior to the prescription date.");
        DEFAULT_REASONS.put(7, "Direct payment made to the empanelled Lab or Hospital cannot be reimbursed directly by Accounts. Please submit your claim with a justification letter to the Administrative Officer-CHSS for approval by the Competent Authority.");
        DEFAULT_REASONS.put(8, "Doctor has not written the Diagnosis/Dosage of the Medicines/Injections in the Prescription form.");
        DEFAULT_REASONS.put(9, "Please obtain endorsement of your AMO (in the back-to-back Form and on the back side of the cash bills) for the treatment taken from the Hospital.");
        DEFAULT_REASONS.put(10, "Treatment taken from an unauthorised/unempanelled Hospital/Lab is not reimbursable under CHSS. If it is an emergency claim, please submit your claim with a justification letter to the Administrative Officer-CHSS for approval by the Competent Authority.");
        DEFAULT_REASONS.put(11, "Annual CHSS Subscription for the calendar year has not been received. Hence unable to process your Medical Claims.");
        DEFAULT_REASONS.put(12, "Please enclose the ORIGINAL Prescription along with the Medical claim/s and forward it to CHSS-Accounts for processing the claim.");
        DEFAULT_REASONS.put(13, "Doctor has not mentioned the duration for the medicines in the Prescription.");
        DEFAULT_REASONS.put(14, "For beneficiaries going on outstation trips, medicines purchased for more than one month require copies of the onward and return journey tickets to be attached along with the medical claim.");
        DEFAULT_REASONS.put(15, "For beneficiaries travelling abroad, please attach copies of both onward and return tickets along with a copy of the VISA when submitting the medical claim.");
        DEFAULT_REASONS.put(16, "There is a delay in purchasing the medicines. As per the rules, medicines must be purchased within SEVEN DAYS from the date of the prescription.");
        DEFAULT_REASONS.put(17, "The beneficiary has attained the age of 25 years. Hence the medical claim cannot be processed if the consultation date is after attaining 25 years of age.");
        DEFAULT_REASONS.put(18, "AMO cannot prescribe medicines for more than ONE Month. Please obtain justification for issuing a prescription for more than ONE Month.");
    }

    private AppDataInitializer() {
    }

    /**
     * Seeds the rejection_reasons table if it is empty.
     *
     * @param rejectionReasonDAO the DAO to use
     */
    public static void seedRejectionReasons(RejectionReasonDAO rejectionReasonDAO) {
        try {
            if (rejectionReasonDAO.count() > 0) {
                return;
            }

            Map<Integer, String> reasons = loadFromJson();
            if (reasons.isEmpty()) {
                reasons = DEFAULT_REASONS;
            }

            int seeded = 0;
            for (Map.Entry<Integer, String> entry : reasons.entrySet()) {
                rejectionReasonDAO.insert(entry.getKey(), entry.getValue());
                seeded++;
            }
            System.out.println("[CHSS] Seeded " + seeded + " rejection reasons.");
        } catch (Exception e) {
            System.err.println("[CHSS] Could not seed rejection reasons (will use live lookup only): " + e.getMessage());
        }
    }

    /**
     * Reads {@code Rejections.json} from the classpath and extracts the
     * objection_points as number -> description. Returns an empty map if the
     * file is missing or cannot be parsed.
     */
    private static Map<Integer, String> loadFromJson() {
        Map<Integer, String> reasons = new LinkedHashMap<>();
        try (InputStream in = AppDataInitializer.class.getClassLoader()
                .getResourceAsStream("Rejections.json")) {
            if (in == null) {
                System.out.println("[CHSS] Rejections.json not found on classpath; using built-in defaults.");
                return reasons;
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            parseObjectionPoints(json, reasons);
        } catch (Exception e) {
            System.err.println("[CHSS] Failed to parse Rejections.json: " + e.getMessage());
        }
        return reasons;
    }

    /**
     * Extracts {"number": N, "description": "..."} objects from the JSON array.
     * The shipped Rejections.json stores only these two keys per object, in that
     * order, so a single combined pattern is sufficient.
     */
    private static void parseObjectionPoints(String json, Map<Integer, String> out) {
        Pattern entryPat = Pattern.compile(
                "\"number\"\\s*:\\s*(\\d+)\\s*,\\s*\"description\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\")");
        Matcher matcher = entryPat.matcher(json);
        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            String description = decodeJsonString(matcher.group(2));
            out.put(number, description);
        }
    }

    /** Unescapes a JSON string literal (quotes, backslashes, escapes, unicode). */
    private static String decodeJsonString(String literal) {
        String s = literal.substring(1, literal.length() - 1);
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                sb.append(c);
                continue;
            }
            char next = s.charAt(++i);
            switch (next) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 < s.length()) {
                        String hex = s.substring(i + 1, i + 5);
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            sb.append('u');
                        }
                    } else {
                        sb.append('u');
                    }
                    break;
                default: sb.append(next); break;
            }
        }
        return sb.toString();
    }
}
