Shader "VRSkyView/MobileCityUnlit"
{
    Properties
    {
        _MainTex ("Base Facade Texture", 2D) = "white" {}
        _Color ("Facade Base Color", Color) = (0.2, 0.25, 0.35, 1)
        _EmissionTex ("Window Emission Map", 2D) = "black" {}
        _EmissionColor ("Window Glow Color", Color) = (1, 0.9, 0.5, 1)
        _EmissionStrength ("Glow Multiplier", Range(0, 3)) = 1.5
    }
    SubShader
    {
        Tags { "RenderType"="Opaque" "Queue"="Geometry" }
        LOD 100

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

            sampler2D _MainTex;
            float4 _MainTex_ST;
            fixed4 _Color;
            sampler2D _EmissionTex;
            float4 _EmissionTex_ST;
            fixed4 _EmissionColor;
            float _EmissionStrength;

            v2f vert (appdata v)
            {
                v2f o;
                o.vertex = UnityObjectToClipPos(v.vertex);
                o.uv = TRANSFORM_TEX(v.uv, _MainTex);
                return o;
            }

            fixed4 frag (v2f i) : SV_Target
            {
                fixed4 baseCol = tex2D(_MainTex, i.uv) * _Color;
                fixed4 emission = tex2D(_EmissionTex, i.uv) * _EmissionColor * _EmissionStrength;
                return baseCol + emission;
            }
            ENDCG
        }
    }
    FallBack "Mobile/Unlit (Supports Lightmap)"
}
