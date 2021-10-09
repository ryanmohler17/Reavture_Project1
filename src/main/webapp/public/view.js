const urlParams = new URLSearchParams(window.location.search);
let id = parseInt(urlParams.get('id'));
let back = urlParams.get('back');

document.addEventListener("DOMContentLoaded", async function () {
    let res = await fetch('/ers/api/requests/' + id);
    let data = res.json();

    console.log(data);
});