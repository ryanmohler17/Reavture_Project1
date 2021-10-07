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
        let parent = document.createElement("div");
        let div = document.createElement("div");
        div.classList.add("alert-" + this.#type);
        parent.classList.add("alert-show");
        if (this.#header) {
            let header = document.createElement("h3");
            header.innerHTML = this.#header;
            div.appendChild(header);
        }
        let p = document.createElement("p");
        p.innerHTML = this.#content;
        div.appendChild(p)
        parent.appendChild(div);
        return parent;
    }

}

function pushAlert(alert, timeout = 5000) {
    let alertCont = alert.getHtml(document);
    document.querySelector("#alerts").appendChild(alertCont);
    window.setTimeout(() => {
        alertCont.classList.remove("alert-show");
    }, 100);
    if (timeout > 0) {
        window.setTimeout(() => {
            retractElm(alertCont);
            window.setTimeout(() => {
                alertCont.remove();
            }, 2000);
            //alertCont.remove();
        }, timeout);
    }
}

function retractElm(elm) {
    elm.style.height = elm.clientHeight + 'px';
    window.setTimeout(() => {
        elm.classList.add('alert-hide');
    }, 100);
}