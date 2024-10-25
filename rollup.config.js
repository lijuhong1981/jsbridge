import nodeResolve from "@rollup/plugin-node-resolve";
import json from '@rollup/plugin-json';
import terser from "@rollup/plugin-terser";
import cleanup from "rollup-plugin-cleanup";

const output = (file, format, sourcemap) => ({
    input: './index.js',
    output: {
        name: 'jsbridge',
        file,
        format,
        sourcemap,
    },
    plugins: [
        nodeResolve({
            browser: true,
            preferBuiltins: false
        }),
        json(),
        !sourcemap ? cleanup() : undefined,
        !sourcemap ? terser() : undefined,
    ],
    // 用来指定代码执行环境的参数，解决this执行undefined问题 
    context: 'window',
});

export default [
    output('./jsbridge/jsbridge.js', 'umd', true),
    output('./jsbridge/jsbridge.min.js', 'umd', false),
    output('./jsbridge/jsbridge.esm.js', 'esm', true),
    output('./jsbridge/jsbridge.esm.min.js', 'esm', false),
]