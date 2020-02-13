<template>
    <v-simple-table>
        <template v-slot:default>
            <thead>
            <tr>
                <th class="text-left">Serial</th>
                <th class="text-left">Issued at</th>
                <th class="text-left">Last used at</th>
                <th class="text-left">Status</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in x509list" :key="item.serial">
                <td v-if="item.name"><i>{{ item.name }}</i></td>
                <td v-else>{{ item.serial }}</td>

                <td>{{ item.issuedAt }}</td>
                <td>{{ item.lastAccess }}</td>
                <td>
                    <v-chip v-if="!item.revoked" small color="green" outlined :close="true" close-icon="mdi-close"
                            v-on:click:close="revokeX509(item)">
                        ACTIVE
                    </v-chip>
                    <v-chip v-if="item.revoked" small label color="red" :title="item.revokedAt" outlined>REVOKED
                    </v-chip>
                </td>
            </tr>
            </tbody>
        </template>
    </v-simple-table>
</template>
<script>
    import axios from 'axios';
    import sweetalert2 from 'sweetalert2';

    export default {
        props: ['x509list'],
        methods: {
            revokeX509(cert) {
                let displayName = cert.serial;
                if (cert.name) displayName = cert.name;

                sweetalert2.fire({
                    title: 'Are you sure?',
                    html: 'Do you really want to revoke <u>' + displayName + '</u>?<br>You cant undo this!',
                    showCancelButton: true,
                    showConfirmButton: true,
                }).then(r => {
                    if (r.value) {
                        axios.delete('/api/x509/' + cert.serial).finally(() => {
                            this.$emit('updated');
                        });
                    }
                });
            }
        }
    }
</script>