let profileImg;

document.addEventListener("DOMContentLoaded", async function () {
    let resp = await fetch('/ers/api/user');
    let data = await resp.json();

    document.querySelector("#fName").value = data.firstName;
    document.querySelector("#lName").value = data.lastName;

    if (data.avatar) {
        document.querySelector("#img-button").classList.add("hidden");
        let img = document.querySelector("#profile-icon");
        img.src = "data:image/png;base64," + data.avatar;
        img.classList.remove("hidden");
    }

    document.querySelector("#img-button").addEventListener('click', function () {
        document.querySelector("#upload-input").click();
    });
    document.querySelector("#profile-icon").addEventListener('click', function () {
        document.querySelector("#upload-input").click();
    });

    let fileInput = document.querySelector("#upload-input");
    fileInput.addEventListener('change', function () {
        let files = fileInput.files;
        for (let i = 0; i < files.length; i++) {
            handleFile(files[i])
        }
    });

    document.querySelector("#submit-button").addEventListener('click', async function () {
        let update = {};
        if (profileImg) {
            update.img = profileImg;
        }
        let firstName = document.querySelector("#fName").value;
        if (firstName !== data.firstName) {
            update.firstName = firstName;
        }
        let lastName = document.querySelector("#lName").value;
        if (lastName !== data.lastName) {
            update.lastName = lastName;
        }

        let oldPassword = document.querySelector("#current-password").value;
        let newPassword = document.querySelector("#new-password").value;
        let repeatPassword = document.querySelector("#repeat-password").value;

        if (oldPassword && newPassword && repeatPassword) {
            if (newPassword !== repeatPassword) {
                let alert = new Alert(AlertType.Failed, "Passwords do not match");
                alert.setHeader("Failed to submit form");
                pushAlert(alert);
                return;
            }

            update.password = {
                "old": oldPassword,
                "new": newPassword
            }
        } else if (oldPassword || newPassword || repeatPassword) {
            let alert = new Alert(AlertType.Failed, "In order to update your password, all password fields need to be set");
            alert.setHeader("Failed to submit form");
            pushAlert(alert);
            return;
        }
        
        if (!Object.keys(update).length) {
            let alert = new Alert(AlertType.Failed, "Cannot update if you don't change anything");
            alert.setHeader("Failed to submit form");
            pushAlert(alert);
            return;
        }

        let patchRes = await fetch('/ers/api/user', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(update)
        });

        if (patchRes.status === 200) {
            let alert = new Alert(AlertType.Success, "Updated your profile");
            pushAlert(alert);
        } else if (patchRes.status === 403) {
            let alert = new Alert(AlertType.Failed, "Invalid password");
            alert.setHeader("Failed to submit form");
            pushAlert(alert);
        } else {
            let alert = new Alert(AlertType.Failed, "Error occured while submitting");
            alert.setHeader("Failed to submit form");
            pushAlert(alert);
        }
    });
});

function handleFile(file) {
    let type = file.type;
    let types = type.split('/');
    if (file.size < 1) {
        return;
    }
    if (types[0] !== "image") {
        let alert = new Alert(AlertType.Failed, "File with name " + file.name + " was not an image!");
        alert.setHeader("Failed to upload file");
        pushAlert(alert);
        return;
    }
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.addEventListener('loadend', () => {
        let base64 = reader.result;
        document.querySelector("#img-button").classList.add("hidden");
        let img = document.querySelector("#profile-icon");
        img.src = base64;
        img.classList.remove("hidden");
        profileImg = base64;
    });
}