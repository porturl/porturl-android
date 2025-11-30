import { useAuth } from 'react-oidc-context';

export const useIsAdmin = () => {
    const auth = useAuth();
    if (!auth.user) return false;

    // Check standard Keycloak role locations
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const realmRoles = (auth.user.profile as any).realm_access?.roles || [];
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const resourceAccess = (auth.user.profile as any).resource_access || {};
    // Check specific client roles if needed, or just look for admin anywhere

    const allRoles = new Set<string>([...realmRoles]);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    Object.values(resourceAccess).forEach((resource: any) => {
        if (resource.roles) {

            resource.roles.forEach((r: string) => allRoles.add(r));
        }
    });

    return allRoles.has('admin') || allRoles.has('ROLE_ADMIN');
};
