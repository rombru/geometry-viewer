export namespace ColorService {
    const goldenRatio = 0.618033988749895;
    const hexwidth = 2;
    let hue = 0;

    function hsvToRgb(h: number, s: number, v: number) {
        let h_i = Math.floor(h * 6),
            f = h * 6 - h_i,
            p = v * (1 - s),
            q = v * (1 - f * s),
            t = v * (1 - (1 - f) * s),
            r = 255,
            g = 255,
            b = 255;
        switch (h_i) {
            case 0:
                r = v; g = t; b = p;
                break;
            case 1:
                r = q; g = v; b = p;
                break;
            case 2:
                r = p; g = v; b = t;
                break;
            case 3:
                r = p; g = q; b = v;
                break;
            case 4:
                r = t; g = p; b = v;
                break;
            case 5:
                r = v; g = p; b = q;
                break;
        }
        return [Math.floor(r * 256), Math.floor(g * 256), Math.floor(b * 256)];
    }

    function padHex(str: string) {
        if (str.length > hexwidth) return str;
        return new Array(hexwidth - str.length + 1).join('0') + str;
    }

    export function get(hex: boolean = true, saturation: number = 0.8, value: number = 0.95) {
        hue += goldenRatio;
        hue %= 1;
        const rgb = hsvToRgb(hue, saturation, value);
        if (hex) return "#" + padHex(rgb[0].toString(16)) + padHex(rgb[1].toString(16)) + padHex(rgb[2].toString(16));
        else return rgb;
    }
}