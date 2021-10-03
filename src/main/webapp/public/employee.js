document.addEventListener('DOMContentLoaded', async function () {
    handleProfile();
    handleReimbersement();
    window.setInterval(() => {
        let d = new Date();
        document.querySelectorAll(".time").forEach(elm => {
            elm.innerHTML = d.toLocaleDateString() + " " + d.toLocaleTimeString();
        })
    }, 100)
});

async function handleProfile() {
    let ret = await fetch("/ers/api/user")
    let data = await ret.json();

    if (data.error) {
        return;
    }

    let info = document.querySelector("#employee-info");
    console.log(data);
    info.querySelector("h3").innerHTML = "Hi " + data.user.firstName;

}

async function handleReimbersement() {
    let ret = await fetch("/ers/api/requests")
    let data = await ret.json();

    if (data.error) {
        return;
    }

    let reimbursement = document.querySelector("#reimbursement")
    console.log(reimbursement);
    let li = reimbursement.querySelectorAll("ul > li > a")[1];
    console.log(li);
    li.innerHTML = "View requests (" + data.open + ")";

    let pOpen = document.createElement("p");
    let pClosed = document.createElement("p");
    let pAll = document.createElement("p");

    pOpen.innerHTML = data.open + " open requests";
    pClosed.innerHTML = (data.approved + data.denied) + " closed requests";
    pAll.innerHTML = data.total + " total requests";
    let info = document.querySelector("#employee-info");
    info.appendChild(pOpen);
    info.appendChild(pClosed);
    info.appendChild(pAll);

}