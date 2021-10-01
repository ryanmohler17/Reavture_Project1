document.addEventListener('DOMContentLoaded', async function () {
    document.querySelectorAll(".nav li").forEach(item => {
        console.log(item);
        if (item.querySelector("a")) {
            item.addEventListener("click", function () {
                window.location.href = item.querySelector("a").href;
            });
        }
    });

    var resp = await fetch("api/user/");
    let data = await resp.json();

    if (data.error) {
        return;
    }

    let nav = document.querySelector("#nav-account");
    nav.innerHTML = "";
    console.log(data);
    let profile = document.createElement("li");
    if (data.user.hasAvatar) {
        let img = document.createElement("img");
        img.src = "/ers/public/upload/icons/" + data.user.id + ".png";
        profile.appendChild(img);
    }

    let link = document.createElement("a");
    link.href = "/ers/profile";
    link.text = data.user.firstName;

    profile.appendChild(link);

    nav.appendChild(profile)

    let logout = document.createElement("li");
    let logoutLink = document.createElement("a");
    logoutLink.href = "/ers/logout"
    logoutLink.text = "Logout"

    logout.appendChild(logoutLink)
    nav.appendChild(logout);
});