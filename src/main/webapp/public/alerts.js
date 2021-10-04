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
            alertCont.remove();
        }, timeout);
    }
}

function retractElm(elm, transition) {
    var style = window.getComputedStyle(elm, null).getPropertyValue('font-size');
    var fontSize = parseFloat(style); 
    let height = elm.clientHeight;
    let amount = height / (transition / 10);
    let font = fontSize / (transition / 10);
    let interval = window.setInterval(() => {
        let set = height - amount;
        let setFont = fontSize = font;
        //console.log("setting height to " + set)
        elm.style.height = set + "px";
        elm.style.fontSize = set + "px";
        height = set;
        fontSize = setFont;
    }, 10);

    window.setTimeout(() => {
        window.clearInterval(interval);
    }, transition);
}