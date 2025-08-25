// axios型定義の最小限の実装
declare module 'axios' {
    export interface AxiosRequestConfig {
        method?: string;
        url?: string;
        baseURL?: string;
        data?: any;
        headers?: any;
        timeout?: number;
        params?: any;
    }

    export interface AxiosResponse<T = any> {
        data: T;
        status: number;
        statusText: string;
        headers: any;
        config: AxiosRequestConfig;
    }

    export interface AxiosError<T = any> {
        config: AxiosRequestConfig;
        code?: string;
        request?: any;
        response?: AxiosResponse<T>;
        isAxiosError: boolean;
        message: string;
    }

    export interface AxiosInstance {
        request<T = any>(config: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        head<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        options<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
        interceptors: {
            request: {
                use(onFulfilled?: (value: AxiosRequestConfig) => AxiosRequestConfig | Promise<AxiosRequestConfig>, onRejected?: (error: any) => any): number;
            };
            response: {
                use(onFulfilled?: (value: AxiosResponse) => AxiosResponse | Promise<AxiosResponse>, onRejected?: (error: any) => any): number;
            };
        };
    }

    export interface AxiosStatic extends AxiosInstance {
        create(config?: AxiosRequestConfig): AxiosInstance;
    }

    const axios: AxiosStatic;
    export default axios;
}
