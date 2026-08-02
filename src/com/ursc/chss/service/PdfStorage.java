package com.ursc.chss.service;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * PdfStorage - the SINGLE place that decides where generated PDF files are
 * written.
 *
 * <p>Every PDF in the module is created under the directory returned by
 * {@link #resolveStorageDir(ServletContext)} (via
 * {@link LetterService#generateLetter}). If Sandesh must store these files
 * somewhere else (a shared/central file area, a fixed drive path, etc.),
 * change ONLY this method - no other file in the module knows about storage
 * locations.
 *
 * <p>=======================================================================
 * PDF STORAGE PLACEHOLDER - EDIT HERE IN THE OFFICE (if needed)
 * ------------------------------------------------------------------------
 * The default below stores PDFs under {@code <webapp>/generated_letters} (the
 * Tomcat user must be able to write there). To use a different folder, return
 * that absolute path instead, for example:
 *
 *     return "D:/CHSS_LETTERS";
 *
 * The directory is created automatically if it does not exist.
 * ========================================================================
 */
public final class PdfStorage {

    private PdfStorage() {
    }

    /**
     * Resolves the directory in which generated PDF files are stored.
     *
     * @param ctx the webapp's {@link ServletContext} (used only to find the
     *            webapp root; ignored if you hardcode a path in the placeholder)
     * @return an absolute directory path
     */
    public static String resolveStorageDir(ServletContext ctx) {
        String realPath = ctx.getRealPath("");
        if (realPath != null) {
            return realPath.endsWith("\\") || realPath.endsWith("/")
                    ? realPath + "generated_letters"
                    : realPath + File.separator + "generated_letters";
        }
        // Last-resort fallback (e.g. webapp not exploded).
        return System.getProperty("java.io.tmpdir") + File.separator + "chss_generated_letters";
    }
}
