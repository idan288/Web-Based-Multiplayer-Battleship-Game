const DEFAULTEXP = 30;

class CookieUtil {

    static GetCookie(cname) {
        let name = cname + '=';
        let decodedCookie = decodeURIComponent(document.cookie);
        let cookieArr = decodedCookie.split(';');

        for (let i = 0; i < cookieArr.length; i++) {
            let ch = cookieArr[i];
            while (ch.charAt(0) === ' ') {
                ch = ch.substring(1);
            }
            if (ch.indexOf(name) === 0) {
                return ch.substring(name.length, ch.length);
            }
        }
        return '';
    }

    static SetCookie(cname, cvalue, exdays) {
        let d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        let expires = 'expires=' + d.toGMTString();
        document.cookie = cname + '=' + cvalue + ';' + expires + ';path=/';
    }

    static CheckCookie(cookieName, val) {
        let name = this.GetCookie(cookieName);
        if (name !== '' && name !== 'undefined') {
            return name; // there is a cookieName cookie.
        }
        // no cookie, create.
        this.SetCookie(cookieName, val, DEFAULTEXP);
        return val;
    }

    static SetCookieOnChange(cookieName, value) {
        let name = this.GetCookie(cookieName);

        if (name !== value) {
            this.SetCookie(cookieName, value, DEFAULTEXP);
        }
    }

}