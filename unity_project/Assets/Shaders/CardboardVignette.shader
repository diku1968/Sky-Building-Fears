Shader "VRSkyView/CardboardVignette"
{
    Properties
    {
        _VignetteColor ("Vignette Color", Color) = (0, 0, 0, 1)
        _Radius ("Vignette Inner Radius", Range(0.1, 1.0)) = 0.55
        _Softness ("Edge Softness", Range(0.01, 0.8)) = 0.35
    }
    SubShader
    {
        Tags { "Queue"="Overlay" "RenderType"="Transparent" "IgnoreProjector"="True" }
        ZWrite Off
        ZTest Always
        Blend SrcAlpha OneMinusSrcAlpha
        Cull Off

        Pass
        {
            CGPROGRAM
            #pragma vertex vert
            #pragma fragment frag
            #include "UnityCG.cginc"

            struct appdata
            {
                float4 vertex : POSITION;
                float2 uv : TEXCOORD0;
            };

            struct v2f
            {
                float2 uv : TEXCOORD0;
                float4 vertex : SV_POSITION;
            };

            fixed4 _VignetteColor;
            float _Radius;
            float _Softness;

            v2f vert (appdata v)
            {
                v2f o;
                o.vertex = UnityObjectToClipPos(v.vertex);
                o.uv = v.uv;
                return o;
            }

            fixed4 frag (v2f i) : SV_Target
            {
                // Radial distance from center (0.5, 0.5)
                float2 uv = i.uv - float2(0.5, 0.5);
                float dist = length(uv);

                // Smoothstep falloff
                float alpha = smoothstep(_Radius, _Radius + _Softness, dist);
                return fixed4(_VignetteColor.rgb, alpha * _VignetteColor.a);
            }
            ENDCG
        }
    }
}
