document.addEventListener('DOMContentLoaded', function () {
    let login = document.querySelector("#login");
    
    login.querySelector('.btn-submit').addEventListener('click', async function (e) {
        e.preventDefault();
        // login = document.querySelector("#login");
        let json = {
            username: login.querySelector('input[name="user"]').value,
            password: login.querySelector('input[name="pass"]').value
        }

        let returned = await fetch("login", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(json)
        });

        let data = await returned.json();
    });
    
});