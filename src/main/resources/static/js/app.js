document.addEventListener('DOMContentLoaded', function () {
    var toast = document.getElementById('toast');
    if (toast) {
        setTimeout(function () {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.5s';
            setTimeout(function () { toast.remove(); }, 500);
        }, 5000);
    }

    initLivePreview();
    initFormValidation();
    initEditMode();

    var staffId = document.getElementById('staffId');
    if (staffId && staffId.value) {
        fetchEmployee(staffId.value);
    }
});

function dismissToast() {
    var toast = document.getElementById('toast');
    if (toast) toast.remove();
}

function fetchEmployee(staffId) {
    fetch('/api/employees/' + encodeURIComponent(staffId))
        .then(function (r) { return r.json(); })
        .then(function (emp) {
            if (emp && typeof selectEmployee === 'function') selectEmployee(emp);
        })
        .catch(function () {});
}

function initLivePreview() {
    var form = document.getElementById('letterForm');
    if (!form) return;

    var inputs = form.querySelectorAll('input, select, textarea');
    for (var i = 0; i < inputs.length; i++) {
        inputs[i].addEventListener('change', updatePreview);
        inputs[i].addEventListener('input', updatePreview);
        inputs[i].addEventListener('keyup', updatePreview);
    }
}

function updatePreview() {
    var preview = document.getElementById('letterPreview');
    if (!preview) return;

    var staffId = document.getElementById('staffId');
    var employeeName = document.getElementById('employeeName');
    var issueDate = document.getElementById('issueDate');
    var amount = document.getElementById('amount');

    if (!staffId || !staffId.value) {
        preview.innerHTML = '<div class="preview-placeholder"><p>Fill in the form to see a live preview of the rejection letter.</p></div>';
        return;
    }

    var name = employeeName ? employeeName.value : '';
    var date = issueDate ? formatDate(issueDate.value) : '______';
    var amt = amount ? amount.value : '______';
    var expenseDates = getExpenseDates();
    var reasons = getReasons();

    var address = [
        document.getElementById('addressLine1') ? document.getElementById('addressLine1').value : '',
        document.getElementById('addressLine2') ? document.getElementById('addressLine2').value : '',
        document.getElementById('locality') ? document.getElementById('locality').value : '',
        document.getElementById('city') ? document.getElementById('city').value : '',
        document.getElementById('pincode') ? document.getElementById('pincode').value : ''
    ].filter(function (x) { return x; }).join(', ');

    var reasonsHtml = '';
    if (reasons.length > 0) {
        reasonsHtml = '<ol>';
        for (var i = 0; i < reasons.length; i++) {
            reasonsHtml += '<li>' + escapeHtml(reasons[i]) + '</li>';
        }
        reasonsHtml += '</ol>';
    }

    preview.innerHTML = '<div class="center">' +
        '<h2>U R Rao Satellite Centre</h2>' +
        '<h3>Finance and Accounts</h3>' +
        '</div>' +
        '<div class="top">' +
        '<div class="left"><strong>Staff No:</strong> ' + escapeHtml(staffId.value) + '</div>' +
        '<div class="right"><strong>Date:</strong> ' + escapeHtml(date) + '</div>' +
        '</div>' +
        '<div class="clear"></div>' +
        '<div class="subject">SUBJECT: Return of CHSS Claim for Reimbursement of Medical Expenses Under CHSS / CSMA Rule</div>' +
        '<div class="body">Your claim(s) towards reimbursement of medical expenses dated ' +
        escapeHtml(expenseDates !== '______' ? expenseDates : date) +
        ' for Rs. ' + escapeHtml(amt) + '/- is/are returned unpassed on account of reason(s) mentioned below:</div>' +
        '<div class="reasons">' + reasonsHtml + '</div>' +
        '<div class="signature"><strong>Senior Accounts Officer</strong></div>' +
        '<div class="to"><strong>To,</strong><br><br>' +
        escapeHtml(name) + '<br>' +
        escapeHtml(address) +
        '</div>';
}

function getExpenseDates() {
    var inputs = document.querySelectorAll('.expense-date');
    var dates = [];
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].value) {
            dates.push(formatDate(inputs[i].value));
        }
    }
    return dates.length > 0 ? dates.join(', ') : '______';
}

function getReasons() {
    var reasons = [];
    var selects = document.querySelectorAll('.reason-select');
    for (var i = 0; i < selects.length; i++) {
        var row = selects[i].closest('.reason-row');
        if (selects[i].value === 'custom') {
            var customInput = row.querySelector('.custom-reason-input');
            if (customInput && customInput.value.trim()) {
                reasons.push(customInput.value.trim());
            }
        } else if (selects[i].value && selects[i].value !== '') {
            var text = selects[i].options[selects[i].selectedIndex];
            if (text) {
                var txt = text.text;
                var dotIndex = txt.indexOf('. ');
                if (dotIndex > 0) {
                    txt = txt.substring(dotIndex + 2);
                }
                reasons.push(txt);
            }
        }
    }
    return reasons;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    var parts = dateStr.split('-');
    if (parts.length === 3) {
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }
    return dateStr;
}

function previewLetter() {
    updatePreview();
    var previewSection = document.querySelector('.preview-section');
    if (previewSection) {
        previewSection.scrollIntoView({ behavior: 'smooth' });
    }
}

function initFormValidation() {
    var form = document.getElementById('letterForm');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        var staffId = document.getElementById('staffId');
        if (!staffId || !staffId.value) {
            e.preventDefault();
            alert('Please search and select an employee first.');
            document.getElementById('staffSearch').focus();
            return;
        }

        var selects = document.querySelectorAll('.reason-select');
        var hasReason = false;
        for (var i = 0; i < selects.length; i++) {
            if (selects[i].value && selects[i].value !== '' && selects[i].value !== 'custom') {
                hasReason = true;
                break;
            }
        }
        if (!hasReason) {
            var customInputs = document.querySelectorAll('.custom-reason-input');
            for (var j = 0; j < customInputs.length; j++) {
                if (customInputs[j].value.trim()) {
                    hasReason = true;
                    break;
                }
            }
        }
        if (!hasReason) {
            e.preventDefault();
            alert('Please select or enter at least one rejection reason.');
            return;
        }
    });
}

function initEditMode() {
    var editModeField = document.getElementById('editMode');
    if (!editModeField || editModeField.value !== 'true') return;

    var idsData = document.getElementById('selectedReasonIdsData');
    if (!idsData || !idsData.textContent) return;

    var ids = idsData.textContent.split(',').filter(function (id) { return id.trim(); });
    if (ids.length === 0) return;

    var firstSelect = document.querySelector('.reason-select');
    if (firstSelect) {
        firstSelect.value = ids[0];
        if (typeof handleReasonChange === 'function') handleReasonChange(firstSelect);
    }

    for (var i = 1; i < ids.length; i++) {
        if (typeof addReason === 'function') addReason(ids[i]);
    }

    updatePreview();
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}
