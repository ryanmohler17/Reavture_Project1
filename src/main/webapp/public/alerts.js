const AlertType = {
    Failed: "bad",
    Success: "good",
    Neutral: "Neutral"
}

class Alert {
    #type;
    #content;
    #header;

    constructor(type, content) {
        this.#type = type;
        this.#content = content;
    }

    setHeader(header) {
        this.#header = header;
    }

    getHtml(document) {
        let div = document.createElement("div");
        div.classList.add("alert-" + this.#type);
        if (this.#header) {
            let header = document.createElement("h3");
            header.innerHTML = this.#header;
            div.appendChild(header);
        }
        let p = document.createElement("p");
        console.log("content: " + this.#content)
        p.innerHTML = this.#content;
        div.appendChild(p)
        return div;
    }

}

function pushAlert(alert, timeout = 5000) {
    let alertCont = alert.getHtml(document);
    document.querySelector("#alerts").appendChild(alertCont);
    if (timeout > 0) {
        window.setTimeout(() => {
            alertCont.classList.add("alert-hide");
            //window.setTimeout(() => {
                alertCont.remove();
            //}, 3000)
        }, timeout);
    }
}