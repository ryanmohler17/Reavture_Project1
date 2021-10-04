let firstDrop = true;
let drop;
let images = [];
let modal = false;
let dropContent;
let parts = [];

document.addEventListener("DOMContentLoaded", function () {
    drop = document.querySelector("#image-drop");
    drop.addEventListener("drop", function (e) {
        e.preventDefault();
        //console.log("drop");
        if (e.dataTransfer.items) {
            for (let i = 0; i < e.dataTransfer.items.length; i++) {
                let item = e.dataTransfer.items[i];
                if (item.kind === 'file') {
                    item = item.getAsFile();
                    handleFile(item);
                }
            }
        }
    });

    dropContent = drop.innerHTML;

    drop.addEventListener("dragend", function (e) {
        e.preventDefault();
    })

    drop.addEventListener("dragover", function (e) {
        e.preventDefault();
    });

    drop.addEventListener("dragenter", function (e) {
        e.preventDefault();
    });

    drop.addEventListener("dragleave", function (e) {
        e.preventDefault();
    });

    let amount = document.querySelector("#amount");
    let select = document.querySelector("#type");
    select.addEventListener('change', function (e) {
        if (e.target.value === "miles") {
            let val = amount.querySelector("input").value;
            amount.innerHTML = "<input style=\"flex: 1;\" type=\"number\" value=\"" + val + "\" />";
        } else {
            let val = amount.querySelector("input").value;
            amount.innerHTML = "$<input style=\"flex: 1;\" type=\"number\" value=\"" + val + "\" />";
        }
    });

    select.value = "select";

    document.querySelector("#desc").value = "";
    document.querySelector("#amount > input").value = "";

    document.querySelector("#add-part").addEventListener('click', function (e) {
        e.stopPropagation();
        toogleModal();
    });

    document.querySelector(".modal").addEventListener('click', function (e) {
        e.stopPropagation();
    });

    document.body.addEventListener('click', function () {
        if (modal) {
            toogleModal();
        }
    });

    let fileInput = document.querySelector("#file-input");
    fileInput.addEventListener('change', function () {
        let files = fileInput.files;
        for (let i = 0; i < files.length; i++) {
            handleFile(files[i])
        }
    });
});

function handleSubmit() {
    let type = document.querySelector("#type").value;
    let amount = parseInt(document.querySelector("#amount > input").value);
    let description = document.querySelector("#desc").value;

    if (type === "select" || !description || !amount) {
        let alert = new Alert(AlertType.Failed, "Please enter amount, description and select a type");
        alert.setHeader("Invalid form");
        pushAlert(alert);
        return;
    }

    let part = {
        type: type,
        description: description,
        amount: amount,
        images: images
    }

    parts.push(part);
    let alert = new Alert(AlertType.Success, "Succesfully added expense")
    pushAlert(alert);
    clearModal();
    toogleModal();
    addPartToTable(part);
}

function addPartToTable(part) {
    let tr = document.createElement("tr");
    let amount = document.createElement("td");
    let desc = document.createElement("td");
    let type = document.createElement("td");

    amount.innerHTML = part.amount;
    desc.innerHTML = part.description;
    type.innerHTML = document.querySelector("option[value=" + part.type + "]").innerHTML;

    tr.appendChild(amount)
    tr.appendChild(type)
    tr.appendChild(desc)

    document.querySelector("#expences > table").appendChild(tr);

    let totalElm = document.querySelector("#total");
    totalElm.innerHTML = parseInt(totalElm.innerHTML) + part.amount;

    let expencesElm = document.querySelector("#expence-amount");
    expencesElm.innerHTML = parseInt(expencesElm.innerHTML) + 1;
}

function toogleModal() {
    document.querySelector('#modal-wrapper').classList.toggle('hidden');
    document.body.classList.toggle('body-overflow');
    modal = !modal;
}

function handleFile(file) {
    let type = file.type;
    let types = type.split('/');
    if (file.size < 1) {
        return;
    }
    if (types[0] !== "image") {
        // TODO maybe show message that file could be uploaded
        return;
    }
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.addEventListener('loadend', () => {
        if (firstDrop) {
            drop.innerHTML = "";
            drop.classList.remove("drop")
            firstDrop = false;
            document.querySelector("#lower-upload").classList.remove("hidden");
        }
        let div = document.createElement("div");
        let dummy = document.createElement("div");
        dummy.classList.add("dummy");
        let img = document.createElement("img");
        div.classList.add("drop-img");
        let base64 = reader.result;
        img.src = base64;
        images.push(base64);
        div.appendChild(dummy);
        div.appendChild(img);
        drop.appendChild(div);
        //console.log(reader.result); // reader.result is a base64 string
    });
}

function handleUploadButton() {
    document.querySelector("#file-input").click();
}

function clearModal() {
    drop.innerHTML = dropContent;
    document.querySelector("#type").value = "select";
    document.querySelector("#amount").innerHTML = "<input style=\"flex: 1;\" type=\"number\" value=\"\" />";
    document.querySelector("#desc").value = "";
    images = [];
    firstDrop = true;
    drop.classList.add("drop");
    document.querySelector("#lower-upload").classList.add("hidden");
}

async function hanldeFinalSubmit() {
    if (parts.length == 0) {
        let alert = new Alert(AlertType.Failed, "Please add at least one expence");
        alert.setHeader("Invalid form");
        pushAlert(alert);
        return;
    }

    let res = fetch("/ers/api/requests", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(parts)
    });
}