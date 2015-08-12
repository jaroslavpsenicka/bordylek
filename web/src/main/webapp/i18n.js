window.i18n = {,
    'Hello world!': 'Hallo Welt!',
    'Hello {name}!': function (data) {
        return 'Hallo ' + data.name + '!';
    }
};