document.getElementById('submitForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const operators = document.getElementById('operators').value;
    const locations = document.getElementById('locations').value;
    const newProducts = document.getElementById('newProducts').value;
    const productsToDelete = document.getElementById('productsToDelete').value;
    const productsToUpdate = document.getElementById('productsToUpdate').value;

    const formData = {
        operators: operators,
        locations: locations,
        newProducts: newProducts,
        productsToDelete: productsToDelete,
        productsToUpdate: productsToUpdate
    };

    fetch('/runtest', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Error submitting test: ' + response.status);
            }
        })
        .then(result => {
            const resultWindow = document.getElementById('resultWindow');

            const responseArea = document.createElement('div');
            responseArea.classList.add('response-area');

            const responseLabel = document.createElement('h2');
            responseLabel.textContent = 'Response';

            responseArea.appendChild(responseLabel);
            responseArea.appendChild(document.createTextNode(result));

            resultWindow.innerHTML = '';
            resultWindow.appendChild(responseArea);
        })
        .catch(error => {
            console.error('Error submitting test:', error);
        });
});