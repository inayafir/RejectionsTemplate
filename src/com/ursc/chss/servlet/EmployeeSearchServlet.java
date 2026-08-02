package com.ursc.chss.servlet;

import com.ursc.chss.listener.AppContextListener;
import com.ursc.chss.model.Employee;
import com.ursc.chss.service.LetterService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Backs the "Search Staff" autocomplete on the generate page.
 *
 * <p>{@code GET /chss/employee-search?q=...} returns a JSON array of matching
 * employees, read directly from the organisation's existing employee table via
 * {@link LetterService#getEmployeeDAO()}.
 *
 * <p>Output is hand-built JSON (no external JSON library) with the exact
 * camelCase keys the JavaScript uses. If the existing Sandesh employee module
 * already exposes a matching lookup, the JavaScript in generate.jsp can be
 * pointed at that instead.
 */
@WebServlet(urlPatterns = "/chss/employee-search")
public class EmployeeSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LetterService service = (LetterService) request.getServletContext()
                .getAttribute(AppContextListener.KEY_LETTER_SERVICE);
        if (service == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "CHSS module not initialised");
            return;
        }

        String query = request.getParameter("q");
        List<Employee> employees = service.getEmployeeDAO().searchEmployees(query);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(toJsonArray(employees));
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
