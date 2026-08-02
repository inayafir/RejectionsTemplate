package com.ursc.chss.listener;

import com.ursc.chss.dao.EmployeeDAO;
import com.ursc.chss.dao.GeneratedLetterDAO;
import com.ursc.chss.dao.RejectionReasonDAO;
import com.ursc.chss.service.AppDataInitializer;
import com.ursc.chss.service.LetterService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Initialises the CHSS module at webapp startup:
 *
 * <ol>
 *   <li>creates the DAOs and the {@link LetterService},</li>
 *   <li>seeds the {@code rejection_reasons} table on first run,</li>
 *   <li>stores shared objects as ServletContext attributes.</li>
 * </ol>
 *
 * <p>Requires Servlet 3.0+ annotation scanning (the default for a normal
 * Eclipse Dynamic Web Project / Tomcat). If the Sandesh web.xml sets
 * {@code metadata-complete="true"}, add the listener there instead - see
 * {@code web/WEB-INF/CHSS_SERVLET_MAPPINGS.txt}.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    /** ServletContext attribute keys shared with the servlets. */
    public static final String KEY_LETTER_SERVICE = "chss.letterService";
    public static final String KEY_EMPLOYEE_DAO = "chss.employeeDao";
    public static final String KEY_REASON_DAO = "chss.reasonDao";
    public static final String KEY_LETTER_DAO = "chss.letterDao";
    public static final String KEY_STORAGE_DIR = "chss.storageDir";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            EmployeeDAO employeeDAO = new EmployeeDAO();
            RejectionReasonDAO rejectionReasonDAO = new RejectionReasonDAO();
            GeneratedLetterDAO generatedLetterDAO = new GeneratedLetterDAO();

            // Bootstrap the static rejection reasons on first startup.
            AppDataInitializer.seedRejectionReasons(rejectionReasonDAO);

            String storageDir = resolveStorageDir(ctx);
            LetterService letterService =
                    new LetterService(employeeDAO, rejectionReasonDAO, generatedLetterDAO);

            ctx.setAttribute(KEY_LETTER_SERVICE, letterService);
            ctx.setAttribute(KEY_EMPLOYEE_DAO, employeeDAO);
            ctx.setAttribute(KEY_REASON_DAO, rejectionReasonDAO);
            ctx.setAttribute(KEY_LETTER_DAO, generatedLetterDAO);
            ctx.setAttribute(KEY_STORAGE_DIR, storageDir);

            ctx.log("[CHSS] Module initialised. PDF storage: " + storageDir);
        } catch (Exception e) {
            ctx.log("[CHSS] Failed to initialise module", e);
            throw new IllegalStateException("CHSS module initialisation failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to release - connections are opened/closed per query.
    }

    /**
     * Resolves the directory in which generated PDF files are stored. Defaults
     * to {@code <webapp>/generated_letters}. Change this if Sandesh uses a
     * shared/central file area instead.
     */
    private String resolveStorageDir(ServletContext ctx) {
        String realPath = ctx.getRealPath("");
        if (realPath != null) {
            return realPath.endsWith("\\") || realPath.endsWith("/")
                    ? realPath + "generated_letters"
                    : realPath + java.io.File.separator + "generated_letters";
        }
        // Last-resort fallback (e.g. WAR not exploded).
        return System.getProperty("java.io.tmpdir") + java.io.File.separator + "chss_generated_letters";
    }
}
