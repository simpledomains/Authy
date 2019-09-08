<template>
    <v-app>
        <v-app-bar app color="indigo" dark>
            <v-icon>fas fa-lock</v-icon>&nbsp; &nbsp;
            <v-toolbar-title>Authentication Service - Access Denied</v-toolbar-title>
        </v-app-bar>

        <v-content>
            <v-container>
                <v-row>
                    <v-flex xs8 offset-xs2 pt-5>
                        <v-alert type="error" border="left" prominent outlined>
                            Oops! You'r request was blocked. It seems you dont have enough power to access this website.<br>
                            If you think i made a wrong decision, contact your system administrator.<br><br>
                            You can also try again <a class="indigo--text" :href="this.$route.query.source">here</a> or you login
                            with another account which has the power to access the website <router-link class="indigo--text" :to="'/login?redirect_uri=' + this.$route.query.source + '&logout=1'">here</router-link>.
                        </v-alert>
                    </v-flex>
                </v-row>
            </v-container>
        </v-content>

        <v-footer color="indigo" app>
            <span class="white--text my-1">&copy; 2019 by SeaLife</span>
        </v-footer>
    </v-app>
</template>

<script>
    export  default  {
        data: () => ({
        }),
        methods: {
        }
    }
</script>