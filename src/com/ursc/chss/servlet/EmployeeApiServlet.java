package com.ursc.chss.servlet;

import com.ursc.chss.dao.EmployeeDAO;
import com.ursc.chss.listener.AppContextListener;
import com.ursc.chss.model.Employee;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * JSON endpoint backing the employee autocomplete on the generate page and the
 * edit-page employee repopulation.
 *
 * <ul>
 *   <li>{@code GET /api/employees/search?q=...} returns a JSON array of matching employees</li>
 *   <li>{@code GET /api/employees/{staffId}} returns a single employee JSON object (or {@code null})</li>
 * </ul>
 *
 * <p>Output is hand-built JSON (no external JSON library) with the exact same
 * camelCase keys the JavaScript uses. If the Sandesh project already serves
 * JSON for employee lookup, these two endpoints can be pointed at that service
 * instead.
 */
@WebServlet(urlPatterns = "/api/employees/*")
public class EmployeeApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EmployeeDAO dao = (EmployeeDAO) request.getServletContext()
                .getAttribute(AppContextListener.KEY_EMPLOYEE_DAO);
        if (dao == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "CHSS module not initialised");
            return;
        }

        String pathInfo = request.getPathInfo(); // "/search", "/ISO0012" or null
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/search")) {
            // /api/employees/search?q=...  (also tolerate a bare /api/employees)
            String query = request.getParameter("q");
            List<Employee> employees = dao.searchEmployees(query);
            out.print(toJsonArray(employees));
            return;
        }

        String staffId = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (staffId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing staff id");
            return;
        }

        Employee employee = dao.findById(staffId);
        out.print(employee == null ? "null" : toJson(employee));
    }

    private String toJsonArray(List<Employee> employees) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < employees.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(employees.get(i)));
        }
        return sb.append("]").toString();
    }

    private String toJson(Employee emp) {
        return "{\"staffId\":" + q(emp.getStaffId()) +
                ",\"employeeName\":" + q(emp.getEmployeeName()) +
                ",\"department\":" + q(emp.getDepartment()) +
                ",\"designation\":" + q(emp.getDesignation()) +
                ",\"phone\":" + q(emp.getPhone()) +
                ",\"email\":" + q(emp.getEmail()) +
                ",\"addressLine1\":" + q(emp.getAddressLine1()) +
                ",\"addressLine2\":" + q(emp.getAddressLine2()) +
                ",\"locality\":" + q(emp.getLocality()) +
                ",\"city\":" + q(emp.getCity()) +
                ",\"pincode\":" + q(emp.getPincode()) + "}";
    }

    /** JSON string literal (null-safe, escaped). */
    private String q(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append("\"").toString();
    }
}
