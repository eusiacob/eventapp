document.querySelectorAll('.service-area-picker').forEach(picker => {
    const nationwide = picker.querySelector('.service-area-nationwide');
    const counties = Array.from(picker.querySelectorAll('.service-area-county'));
    const options = picker.querySelector('.service-area-options');
    const count = picker.querySelector('.service-area-count');
    const hint = picker.querySelector('.service-area-hint');

    function update() {
        const selected = counties.filter(input => input.checked).length;
        counties.forEach(input => {
            input.disabled = nationwide.checked || (selected >= 10 && !input.checked);
            input.setCustomValidity('');
        });
        count.textContent = `(${selected}/10)`;
        hint.textContent = nationwide.checked
            ? 'Serviciul va fi disponibil pentru căutări în toate județele.'
            : selected >= 10
                ? 'Ai selectat 10 județe. Debifează unul pentru a alege altul.'
                : 'Poți selecta maximum 10 județe, inclusiv București.';
        if (!nationwide.checked && selected === 0) {
            counties[0].setCustomValidity('Alege cel puțin un județ sau Toată țara.');
        }
        options.hidden = nationwide.checked;
    }

    nationwide.addEventListener('change', () => {
        if (nationwide.checked) counties.forEach(input => { input.checked = false; });
        else options.open = true;
        update();
    });
    counties.forEach(input => input.addEventListener('change', update));
    picker.addEventListener('invalid', () => { options.open = true; }, true);
    update();
});
