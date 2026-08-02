<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Rejection Letter - CHSS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <div class="app-container">
        <% if (request.getAttribute("success") != null) { %>
            <div class="toast toast-success" id="toast">
                <span>${success}</span>
                <button onclick="dismissToast()" class="toast-close">&times;</button>
            </div>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
            <div class="toast toast-error" id="toast">
                <span>${error}</span>
                <button onclick="dismissToast()" class="toast-close">&times;</button>
            </div>
        <% } %>

        <main class="main-content">
            <div class="page-header">
                <h1>Create Rejection Letter</h1>
            </div>

            <form action="${pageContext.request.contextPath}/generate" method="post" id="letterForm" class="form-preview-grid">
                <input type="hidden" name="employeeName" id="employeeName" value="${letterForm.employeeName}" />
                <input type="hidden" name="addressLine1" id="addressLine1" value="${letterForm.addressLine1}" />
                <input type="hidden" name="addressLine2" id="addressLine2" value="${letterForm.addressLine2}" />
                <input type="hidden" name="locality" id="locality" value="${letterForm.locality}" />
                <input type="hidden" name="city" id="city" value="${letterForm.city}" />
                <input type="hidden" name="pincode" id="pincode" value="${letterForm.pincode}" />

                <div class="form-section">
                    <div class="card form-card">
                        <div class="card-body">
                            <div class="section-title">Employee Details</div>
                            <div class="form-group">
                                <label for="staffSearch">Search Staff</label>
                                <div class="autocomplete-wrapper">
                                    <input type="text" id="staffSearch" class="form-control"
                                           placeholder="Type Staff ID or name..."
                                           autocomplete="off" />
                                    <input type="hidden" name="staffId" id="staffId" value="${letterForm.staffId}" />
                                    <div id="autocompleteResults" class="autocomplete-results"></div>
                                </div>
                            </div>
                            <div class="employee-card${(not empty letterForm.staffId || editMode) ? ' show' : ''}" id="employeeDetails">
                                <div class="emp-row">
                                    <span class="emp-label">Name:</span>
                                    <span class="emp-value" id="displayName">${letterForm.employeeName}</span>
                                </div>
                                <div class="emp-row">
                                    <span class="emp-label">Address:</span>
                                    <span class="emp-value" id="displayAddress">${addressPreview}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card form-card">
                        <div class="card-body">
                            <div class="section-title">Letter Details</div>
                            <div class="form-group">
                                <label for="issueDate">Date of Issue *</label>
                                <input type="date" name="issueDate" id="issueDate" class="form-control"
                                       value="${letterForm.issueDate}" required />
                            </div>
                            <div class="form-group">
                                <label>Medical Expense Date(s)</label>
                                <div class="expense-dates-container" id="expenseDatesContainer">
                                    <div class="input-group">
                                        <input type="date" name="medicalExpenseDates" class="form-control expense-date" />
                                        <button type="button" class="btn-icon add-btn" onclick="addExpenseDate()" title="Add another date">+</button>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="amount">Amount (Rs.) *</label>
                                <div class="input-group">
                                    <span class="input-prefix">Rs.</span>
                                    <input type="number" step="0.01" name="amount" id="amount" class="form-control"
                                           value="${letterForm.amount}" placeholder="Enter amount" required />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card form-card">
                        <div class="card-body">
                            <div class="section-title">Rejection Reasons</div>
                            <input type="hidden" id="editMode" value="${editMode ? 'true' : 'false'}" />
                            <textarea id="selectedReasonIdsData" style="display:none;"><%= request.getAttribute("selectedReasonIdsCsv") == null ? "" : String.valueOf(request.getAttribute("selectedReasonIdsCsv")) %></textarea>
                            <div class="reasons-section" id="reasonsContainer">
                                <div class="reason-row" data-index="0">
                                    <div class="reason-select-wrapper">
                                        <select name="selectedReasonIds" class="form-control reason-select" onchange="handleReasonChange(this)">
                                            <option value="">-- Select Reason --</option>
                                            <%
                                                java.util.List<com.ursc.chss.model.RejectionReason> reasons =
                                                        (java.util.List<com.ursc.chss.model.RejectionReason>) request.getAttribute("rejectionReasons");
                                                if (reasons != null) {
                                                    for (com.ursc.chss.model.RejectionReason reason : reasons) {
                                            %>
                                            <option value="<%= reason.getId() %>"><%= reason.getReasonNumber() %>. <%= reason.getDescription() %></option>
                                            <%
                                                    }
                                                }
                                            %>
                                            <option value="custom">-- Custom Reason --</option>
                                        </select>
                                        <button type="button" class="btn-icon add-btn" onclick="addReason()" title="Add another reason">+</button>
                                    </div>
                                    <div class="custom-reason-wrapper">
                                        <input type="text" name="customReasons" class="form-control custom-reason-input"
                                               placeholder="Type custom reason..."
                                               oninput="updatePreview()" />
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="actions-bar">
                            <button type="button" class="btn btn-secondary" onclick="previewLetter()">Preview</button>
                            <button type="submit" class="btn btn-yellow">Generate PDF</button>
                        </div>
                    </div>
                </div>

                <div class="preview-section">
                    <div class="card preview-card">
                        <div class="card-body">
                            <div class="section-title">Live Preview</div>
                        </div>
                        <div class="preview-body" id="previewContainer">
                            <div class="letter-preview" id="letterPreview">
                                <div class="preview-placeholder">
                                    <p>Fill in the form to see a live preview of the rejection letter.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </main>
    </div>

    <script>window.CONTEXT_PATH = '${pageContext.request.contextPath}';</script>
    <script src="${pageContext.request.contextPath}/js/app.js?v=5"></script>
    <script>
    (function() {
        var input = document.getElementById('staffSearch');
        if (!input) return;
        var resultsContainer = document.getElementById('autocompleteResults');
        var debounceTimer;

        input.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            var query = this.value.trim();
            if (query.length < 1) {
                resultsContainer.classList.remove('show');
                resultsContainer.innerHTML = '';
                return;
            }
            debounceTimer = setTimeout(function () {
                fetch(CONTEXT_PATH + '/employee-search?q=' + encodeURIComponent(query))
                    .then(function (r) { return r.json(); })
                    .then(function (data) {
                        resultsContainer.innerHTML = '';
                        if (!data || data.length === 0) {
                            resultsContainer.classList.remove('show');
                            return;
                        }
                        data.forEach(function (emp) {
                            var item = document.createElement('div');
                            item.className = 'autocomplete-item';
                            item.innerHTML = '<div class="emp-id">' + escapeHtml(emp.staffId) + '</div>' +
                                             '<div class="emp-name">' + escapeHtml(emp.employeeName || '') + '</div>';
                            item.addEventListener('mousedown', function (e) {
                                e.preventDefault();
                                selectEmployee(emp);
                            });
                            resultsContainer.appendChild(item);
                        });
                        resultsContainer.classList.add('show');
                    })
                    .catch(function () {
                        resultsContainer.classList.remove('show');
                    });
            }, 300);
        });

        input.addEventListener('blur', function () {
            setTimeout(function () {
                resultsContainer.classList.remove('show');
            }, 250);
        });

        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                var first = resultsContainer.querySelector('.autocomplete-item');
                if (first) first.click();
            }
        });
    })();

    function selectEmployee(emp) {
        document.getElementById('staffSearch').value = emp.staffId + ' - ' + (emp.employeeName || '');
        document.getElementById('staffId').value = emp.staffId;
        document.getElementById('employeeName').value = emp.employeeName || '';
        document.getElementById('addressLine1').value = emp.addressLine1 || '';
        document.getElementById('addressLine2').value = emp.addressLine2 || '';
        document.getElementById('locality').value = emp.locality || '';
        document.getElementById('city').value = emp.city || '';
        document.getElementById('pincode').value = emp.pincode || '';

        var card = document.getElementById('employeeDetails');
        card.classList.add('show');
        document.getElementById('displayName').textContent = emp.employeeName || '';

        var address = [emp.addressLine1, emp.addressLine2, emp.locality, emp.city, emp.pincode]
            .filter(function (x) { return x; })
            .join(', ');
        document.getElementById('displayAddress').textContent = address || 'No address on record';

        document.getElementById('autocompleteResults').classList.remove('show');
        if (typeof updatePreview === 'function') updatePreview();
    }

    function addExpenseDate() {
        var container = document.getElementById('expenseDatesContainer');
        var wrapper = document.createElement('div');
        wrapper.className = 'input-group';
        wrapper.innerHTML = '<input type="date" name="medicalExpenseDates" class="form-control expense-date" />' +
                            '<button type="button" class="btn-icon danger-btn" onclick="this.parentElement.remove(); updatePreview();" title="Remove date">x</button>';
        container.appendChild(wrapper);
        if (typeof updatePreview === 'function') updatePreview();
    }

    function addReason(selectedId) {
        var container = document.getElementById('reasonsContainer');
        var index = container.children.length;
        var wrapper = document.createElement('div');
        wrapper.className = 'reason-row';
        wrapper.setAttribute('data-index', index);

        var optionsHtml = '<option value="">-- Select Reason --</option>';
        var selects = document.querySelectorAll('.reason-select');
        if (selects.length > 0) {
            var firstSelect = selects[0];
            for (var i = 0; i < firstSelect.options.length; i++) {
                var opt = firstSelect.options[i];
                var selected = (opt.value === String(selectedId)) ? 'selected' : '';
                optionsHtml += '<option value="' + escapeHtml(opt.value) + '" ' + selected + '>' +
                               escapeHtml(opt.text) + '</option>';
            }
        }

        wrapper.innerHTML = '<div class="reason-select-wrapper">' +
                            '<select name="selectedReasonIds" class="form-control reason-select" onchange="handleReasonChange(this)">' +
                            optionsHtml +
                            '</select>' +
                            '<button type="button" class="btn-icon danger-btn" onclick="this.parentElement.parentElement.remove(); updatePreview();" title="Remove reason">x</button>' +
                            '</div>' +
                            '<div class="custom-reason-wrapper">' +
                            '<input type="text" name="customReasons" class="form-control custom-reason-input" placeholder="Type custom reason..." oninput="updatePreview()" />' +
                            '</div>';

        container.appendChild(wrapper);
        if (typeof updatePreview === 'function') updatePreview();
    }

    function handleReasonChange(select) {
        var row = select.closest('.reason-row');
        var customWrapper = row.querySelector('.custom-reason-wrapper');
        if (select.value === 'custom') {
            customWrapper.classList.add('show');
        } else {
            customWrapper.classList.remove('show');
        }
        if (typeof updatePreview === 'function') updatePreview();
    }
    </script>
</body>
</html>
