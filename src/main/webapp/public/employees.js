document.addEventListener("DOMContentLoaded", async function () {
    document.querySelector("#create").addEventListener('click', function (e) {
        e.stopPropagation();
        toggleModal('create-modal');
    });

    document.querySelector("#add").addEventListener('click', async function () {
        let userName = document.querySelector("#username").value;
        let email = document.querySelector("#email").value;
        let fName = document.querySelector("#fName").value;
        let lName = document.querySelector("#lName").value;
        let password = document.querySelector("#password").value;
        let repeat = document.querySelector("#repeat").value;

        if (!userName || !email || !fName || !lName || !password || !repeat) {
            let alert = new Alert(AlertType.Failed, "Please enter everything");
            alert.setHeader("Invalid form");
            pushAlert(alert);
            return;
        }

        if (password != repeat) {
            let alert = new Alert(AlertType.Failed, "Passwords do not match");
            alert.setHeader("Invalid form");
            pushAlert(alert);
            return;
        }

        let data = {
            firstName: fName,
            lastName: lName,
            userName: userName,
            email: email,
            password: password
        }

        let returned = await fetch("/ers/api/user", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (returned.status === 200) {
            let alert = new Alert(AlertType.Success, "Created new user");
            pushAlert(alert);
            createEmployeeDiv(data);
        } else if (returned.status == 401) {
            let alert = new Alert(AlertType.Failed, "You are not logged in");
            alert.setHeader("Error submitting form");
            pushAlert(alert);
        } else if (returned.status === 403) {
            let alert = new Alert(AlertType.Failed, "You do not have permission to do this");
            alert.setHeader("Error submitting form");
            pushAlert(alert);
        } else {
            let alert = new Alert(AlertType.Failed, "An error occured while submitting the form");
            alert.setHeader("Error submitting form");
            pushAlert(alert);
        }
        toggleModal('create-modal');
        document.querySelector("#username").value = "";
        document.querySelector("#email").value = "";
        document.querySelector("#fName").value = "";
        document.querySelector("#lName").value = "";
        document.querySelector("#password").value= "";
        document.querySelector("#repeat").value= "";
    });

    let resp = await fetch('/ers/api/users');
    let data = await resp.json();

    for (let i = 0; i < data.length; i++) {
        let emp = data[i];
        createEmployeeDiv(emp);
    }
});

function createEmployeeDiv(emp) {
    let div = document.createElement("div");
    div.classList.add("row");
    div.classList.add("card");
    let info = document.createElement("div");
    let h3 = document.createElement("h3");
    h3.innerHTML = emp.firstName + " " + emp.lastName;
    info.appendChild(h3);
    let userP = document.createElement("p");
    userP.innerHTML = emp.userName;
    info.appendChild(userP);
    let emailP = document.createElement("p");
    emailP.innerHTML = emp.email;
    info.appendChild(emailP);
    div.appendChild(info);
    if (emp.avatar) {
        let img = document.createElement("img");
        img.style.maxWidth = "5em";
        img.style.maxHeight = "5em";
        img.style.border = "2px solid black";
        img.style.borderRadius = "5em";
        img.src = "data:image/png;base64," + emp.avatar;

        div.appendChild(img);
    }

    document.querySelector("#employees").appendChild(div);
}