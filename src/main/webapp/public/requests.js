let gotAll = false;
let employeeCache = {};
let manager = false;
let emp = "all";
let status = "all";
document.addEventListener("DOMContentLoaded", async function () {
    var resp = await fetch("/ers/api/user/");
    let data = await resp.json();

    if (data.userType === "MANAGER") {
        manager = true;
        document.querySelector("#select-employee").classList.remove("hidden");
        let th = document.createElement("th");
        th.innerHTML = "Employee";
        document.querySelector("#requests-head").appendChild(th);
    }

    while (!gotAll) {
        await fetchRequests(5);
    }
    let loader = document.querySelector("#loader");
    loader.classList.remove('loader');
    loader.classList.add('hidden');
});

let gotAmount = 0;
async function fetchRequests(amount) {
    let url = "/ers/api/requests?limit=" + amount;
    if (gotAmount > 0) {
        url += ("&offset=" + gotAmount);
    }
    let res = await fetch(url, {
        headers: {
            'Content-Type': 'application/json'
        }
    });

    let data = await res.json();
    gotAmount += data.length;
    if (data.length < amount) {
        gotAll = true;
    }

    let table = document.querySelector("#requests");
    for (let i = 0; i < data.length; i++) {
        let item = data[i];
        let tr = document.createElement("tr");

        let amount = document.createElement("td");
        let partTotal = 0;
        for (let i2 = 0; i2 < item.parts.length; i2++) {
            let part = item.parts[i2];
            if (part.type === "TRAVEL_MILES") {
                partTotal += (part.amount * part.rate);
            } else {
                partTotal += part.amount;
            }
        }
        amount.innerHTML = "$" + partTotal;

        let parts = document.createElement("td");
        parts.innerHTML = item.parts.length;

        let statusElm = document.createElement("td");
        statusElm.innerHTML = item.status;
        statusElm.classList.add("status");

        let submitted = document.createElement("td");
        submitted.innerHTML = item.submitted;

        let controlls = document.createElement("td");
        let viewButton = document.createElement("button");
        viewButton.innerHTML = "View"
        viewButton.classList.add("btn");
        viewButton.classList.add("btn-primary");
        viewButton.addEventListener('click', () => {
            window.location.href = '/ers/view/?id=' + encodeURIComponent(item.id) + "&back=" + encodeURIComponent("/ers/requests");
        });
        controlls.appendChild(viewButton);
        // TODO controlls

        tr.appendChild(amount);
        tr.appendChild(parts);
        tr.appendChild(statusElm);
        tr.appendChild(submitted);
        tr.appendChild(controlls);

        if (manager) {
            let name = "";
            if (employeeCache.hasOwnProperty(item.employee)) {
                name = employeeCache[item.employee];
            } else {
                let resp = await fetch('/ers/api/user/' + item.employee);
                let respData = await resp.json();

                name = respData.firstName + " " + respData.lastName;
                employeeCache[item.employee] = name;

                let empSel = document.createElement("option");
                empSel.value = item.employee;
                empSel.innerHTML = name;
                document.querySelector("#select-employee > select").appendChild(empSel);
            }
            let empElm = document.createElement("td");
            empElm.innerHTML = name;
            empElm.id = "#emp-" + item.employee;
            empElm.classList.add("emp");
            tr.appendChild(empElm);
        }

        table.appendChild(tr);

        if (emp != "all") {
            if (item.employee != emp) {
                tr.classList.add("hidden");
            }
        }

        if (status != "all") {
            if (item.status.toLocaleLowerCase() != status) {
                tr.classList.add("hidden");
            }
        }
    }

}

function searchStatus(e) {
    status = e.value;
    search();
}

function searchEmployee(e) {
    emp = e.value;
    search();
}

function search() {
    let rows = document.querySelectorAll("#requests tr");
    rows.forEach(row => {
        row.classList.remove("hidden");
        if (row.id == "requests-head") {
            return;
        }
        if (emp != "all" && manager) {
            let empElm = row.querySelector(".emp");
            let id = empElm.id.split("-")[1];
            if (id != emp) {
                row.classList.add("hidden");
            }
        }
        if (status != "all") {
            let statusElm = row.querySelector(".status");
            if (statusElm.innerHTML.toLocaleLowerCase() != status) {
                row.classList.add("hidden");
            }
        }
    });
}