document.addEventListener('DOMContentLoaded', async function () {
    let ret = await fetch("/ers/api/requests")
    let data = await ret.json();

    if (data.error) {
        return;
    }

    let reimbursement = document.querySelector("#reimbursement")
    let li = reimbursement.ul[1];
    li.text = "View requests (" + data.open + ")"
});