const urlParams = new URLSearchParams(window.location.search);
let id = parseInt(urlParams.get('id'));
let back = urlParams.get('back');

let manager = false;

document.addEventListener("DOMContentLoaded", async function () {
    document.querySelector("#back-button").addEventListener('click', function () {
        window.location.href = back;
    });

    var userRes = await fetch("/ers/api/user/");
    let userData = await userRes.json();
    if (userData.userType === "MANAGER") {
        manager = true;
        document.querySelector("#manager-buttons").classList.remove("hidden");
        document.querySelector("#status-head").classList.add("hidden");
    }

    let res = await fetch('/ers/api/requests/' + id);
    let data = await res.json();

    document.querySelector("#status").value = data.status;

    document.querySelector("#status-head > span").innerHTML = data.status;

    let infoElm = document.querySelector("#totals");

    let partCountElm = createCard("Parts", data.parts.length);
    infoElm.appendChild(partCountElm);

    let submittedElm = createCard("Submitted on", data.submitted);
    infoElm.appendChild(submittedElm);

    let empRes = await fetch('/ers/api/user/' + data.employee);
    let empData = await empRes.json();

    let submittedByElm = createCard("Submitted By", empData.firstName + " " + empData.lastName);
    infoElm.appendChild(submittedByElm);

    if (data.resolvedBy != -1) {
        let manRes = await fetch('/ers/api/user/' + data.resolvedBy);
        let manData = await manRes.json();

        let resolvedByElm = createCard("Resolved By", manData.firstName + " " + manData.lastName);
        infoElm.appendChild(resolvedByElm);
    }

    if (data.lastUpdate) {
        let lastUpdateElm = createCard("Updated", data.lastUpdate);
        infoElm.appendChild(lastUpdateElm);
    }

    let total = 0;
    for (let i = 0; i < data.parts.length; i++) {
        let part = data.parts[i];
        
        let amount = part.amount;
        if (part.type === "TRAVEL_MILES") {
            amount *= part.rate;
        }
        total += amount;

        partElm = createPartElm(part, i);
        document.querySelector("#parts").appendChild(partElm);
    }

    let amountElm = createCard("Amount", "$" + total);
    infoElm.appendChild(amountElm);

    console.log(data);
});

function createPartElm(part, i) {
    let elm = document.createElement("div");
    elm.classList.add("card");
    let header = document.createElement("h3");
    header.innerHTML = "Part " + (i + 1);
    elm.appendChild(header);
    let row = document.createElement("div");
    row.classList.add("row");
    let amountDiv = document.createElement("div");
    let amountHeader = document.createElement("h4");
    amountHeader.innerHTML = "Amount";
    amountDiv.appendChild(amountHeader);
    let amountSpan = document.createElement("span");
    let rateDiv;
    if (part.type != "TRAVEL_MILES") {
        amountSpan.innerHTML = "$" + part.amount;
    } else {
        amountSpan.innerHTML = part.amount;
        rateDiv = document.createElement("div");
        let rateHeader = document.createElement("h4");
        rateHeader.innerHTML = "Rate";
        rateDiv.appendChild(rateHeader);
        let rateSpan = document.createElement("span");
        rateSpan.innerHTML = "$" + part.rate;
        rateDiv.appendChild(rateSpan);
    }
    amountDiv.appendChild(amountSpan);
    row.appendChild(amountDiv);
    if (rateDiv) {
        row.appendChild(rateDiv);
    }

    let typeDiv = document.createElement("div");
    let typeHeader = document.createElement("h4");
    typeHeader.innerHTML = "Type";
    typeDiv.appendChild(typeHeader);
    let typeSpan = document.createElement("span");
    typeSpan.innerHTML = part.type;
    typeDiv.appendChild(typeSpan);

    row.appendChild(typeDiv);
    elm.appendChild(row);

    let descriptionDiv = document.createElement("div");
    let descriptionHeader = document.createElement("h3");
    descriptionHeader.innerHTML = "Description";
    descriptionDiv.appendChild(descriptionHeader);
    let desctiptionP = document.createElement("p");
    desctiptionP.innerHTML = part.description;
    descriptionDiv.appendChild(desctiptionP);
    elm.appendChild(descriptionDiv);

    if (part.images.length > 0) {
        let modal = createModal(part.images, part.id);
        document.querySelector("#modals").appendChild(modal);

        let buttonDiv = document.createElement("div");
        buttonDiv.classList.add("form-buttons");
        let modalButton = document.createElement("button");
        modalButton.classList.add("btn");
        modalButton.classList.add("btn-primary");
        modalButton.innerHTML = "Images";
        modalButton.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleModal('modal-' + part.id);
        });
        buttonDiv.appendChild(modalButton);

        elm.appendChild(buttonDiv);
    }

    return elm;
}

function createModal(images, id) {
    let elm = document.createElement("div");
    elm.classList.add("modal");
    elm.classList.add("hidden");
    elm.id = "modal-" + id;
    elm.addEventListener('click', function (e) {
        e.stopPropagation();
    });
    let content = document.createElement("content");
    elm.style.padding = "1em";
    let header =  document.createElement("h2");
    header.style.marginTop = "0";
    header.innerHTML = "Images";
    content.appendChild(header);


    for(let i = 0; i < images.length; i++) {
        let img = document.createElement("img");
        img.src = "data:image/png;base64," + images[i];
        content.appendChild(img);
    }

    elm.appendChild(content);

    return elm;
}

function createCard(header, text) {
    let cardElm = document.createElement("div");
    cardElm.classList.add('card');
    let cardHeader = document.createElement("h3");
    cardHeader.innerHTML = header;
    cardElm.appendChild(cardHeader);
    let cardSpan = document.createElement("span");
    cardSpan.innerHTML = text;
    cardElm.appendChild(cardSpan);

    return cardElm;
}

async function change() {
    if(manager) {
        let status = document.querySelector("#status").value;
        let json = {
            "status": status,
            "req": id
        }

        let returned = await fetch("/ers/api/requests/status", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(json)
        });

        let data = await returned.json();

        if (returned.status === 200) {
            let alert = new Alert(AlertType.Success, "Successfuly updated the status");
            pushAlert(alert);
            window.setTimeout(() => {
                location.reload();
            }, 6000);
        } else {
            let alert = new Alert(AlertType.Success, data.error);
            alert.setHeader("Failed to update status");
            pushAlert(alert);
        }
    }
}